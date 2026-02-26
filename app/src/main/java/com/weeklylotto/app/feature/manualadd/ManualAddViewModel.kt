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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ManualAddUiState(
    val selected: List<Int> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null,
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
            state.copy(selected = selected.sorted(), error = null)
        }
    }

    fun clear() {
        _uiState.update { it.copy(selected = emptyList(), error = null) }
    }

    fun autoFill() {
        _uiState.update { state ->
            if (state.selected.size >= 6) return@update state
            val remaining = (1..45).filterNot { it in state.selected }.shuffled().take(6 - state.selected.size)
            state.copy(selected = (state.selected + remaining).sorted(), error = null)
        }
    }

    fun save() {
        val selected = uiState.value.selected
        if (selected.size != 6) return

        viewModelScope.launch {
            val targetSignature = selected.sorted()
            val hasDuplicate =
                ticketRepository
                    .observeCurrentRoundTickets()
                    .first()
                    .flatMap { it.games }
                    .any { game -> game.numbers.map { it.value }.sorted() == targetSignature }
            if (hasDuplicate) {
                _uiState.update { it.copy(saved = false, error = "이미 이번 주에 동일 번호가 있습니다.") }
                return@launch
            }

            val today = LocalDate.now()
            val drawDate = RoundEstimator.nextDrawDate(today)
            ticketRepository.save(
                TicketBundle(
                    round = Round(RoundEstimator.currentSalesRound(today), drawDate),
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
            _uiState.update { it.copy(saved = true, error = null) }
        }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }
}
