package com.weeklylotto.app.feature.manualadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class ManualAddUiState(
    val selected: List<Int> = emptyList(),
    val saved: Boolean = false,
)

class ManualAddViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()

    fun toggleNumber(number: Int) {
        _uiState.update { state ->
            val selected = state.selected.toMutableList()
            when {
                selected.contains(number) -> selected.remove(number)
                selected.size < 6 -> selected.add(number)
            }
            state.copy(selected = selected.sorted())
        }
    }

    fun clear() {
        _uiState.update { it.copy(selected = emptyList()) }
    }

    fun autoFill() {
        _uiState.update { state ->
            if (state.selected.size >= 6) return@update state
            val remaining = (1..45).filterNot { it in state.selected }.shuffled().take(6 - state.selected.size)
            state.copy(selected = (state.selected + remaining).sorted())
        }
    }

    fun save() {
        val selected = uiState.value.selected
        if (selected.size != 6) return

        viewModelScope.launch {
            val drawDate = nextSaturday(LocalDate.now())
            ticketRepository.save(
                TicketBundle(
                    round = Round(RoundEstimator.estimate(drawDate), drawDate),
                    source = TicketSource.MANUAL,
                    games =
                        listOf(
                            LottoGame(
                                slot = GameSlot.A,
                                numbers = selected.map(::LottoNumber),
                                lockedNumbers = selected.map(::LottoNumber).toSet(),
                                mode = GameMode.MANUAL,
                            ),
                        ),
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    private fun nextSaturday(from: LocalDate): LocalDate {
        var candidate = from
        while (candidate.dayOfWeek != DayOfWeek.SATURDAY) {
            candidate = candidate.plusDays(1)
        }
        return candidate
    }
}
