package com.weeklylotto.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val currentRound: Int,
    val drawDate: LocalDate,
    val dDay: Int,
    val bundles: List<TicketBundle> = emptyList(),
) {
    companion object {
        fun initial(): HomeUiState {
            val today = LocalDate.now()
            val drawDate = RoundEstimator.nextDrawDate(today)
            return HomeUiState(
                currentRound = RoundEstimator.currentSalesRound(today),
                drawDate = drawDate,
                dDay = java.time.temporal.ChronoUnit.DAYS.between(today, drawDate).toInt(),
            )
        }
    }
}

class HomeViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState.initial())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ticketRepository.observeCurrentRoundTickets().collect { bundles ->
                _uiState.update { state -> state.copy(bundles = bundles) }
            }
        }
    }
}
