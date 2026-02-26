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
    val pendingGames: List<List<Int>> = emptyList(),
    val repeatCount: Int = 1,
    val saved: Boolean = false,
    val savedGameCount: Int = 0,
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

    fun clearPendingGames() {
        _uiState.update { it.copy(pendingGames = emptyList(), error = null) }
    }

    fun setRepeatCount(value: Int) {
        _uiState.update { it.copy(repeatCount = value.coerceIn(1, 5), error = null) }
    }

    fun removePendingGame(index: Int) {
        _uiState.update { state ->
            if (index !in state.pendingGames.indices) return@update state
            state.copy(
                pendingGames =
                    state.pendingGames
                        .toMutableList()
                        .also { it.removeAt(index) },
                error = null,
            )
        }
    }

    fun addSelectedGame() {
        _uiState.update { state ->
            val selected = state.selected
            if (selected.size != 6) {
                return@update state.copy(error = "6개 번호를 먼저 선택하세요.")
            }
            if (state.pendingGames.size >= MAX_MANUAL_GAMES) {
                return@update state.copy(error = "최대 ${MAX_MANUAL_GAMES}게임까지 추가할 수 있습니다.")
            }
            state.copy(
                pendingGames = state.pendingGames + listOf(selected.sorted()),
                error = null,
            )
        }
    }

    fun addSelectedGameRepeated() {
        _uiState.update { state ->
            val selected = state.selected
            if (selected.size != 6) {
                return@update state.copy(error = "6개 번호를 먼저 선택하세요.")
            }
            val remaining = MAX_MANUAL_GAMES - state.pendingGames.size
            if (remaining <= 0) {
                return@update state.copy(error = "최대 ${MAX_MANUAL_GAMES}게임까지 추가할 수 있습니다.")
            }
            val repeat = state.repeatCount.coerceAtMost(remaining)
            state.copy(
                pendingGames = state.pendingGames + List(repeat) { selected.sorted() },
                error = null,
            )
        }
    }

    fun autoFill() {
        _uiState.update { state ->
            if (state.selected.size >= 6) return@update state
            val remaining = (1..45).filterNot { it in state.selected }.shuffled().take(6 - state.selected.size)
            state.copy(selected = (state.selected + remaining).sorted(), error = null)
        }
    }

    fun save() {
        val state = uiState.value
        val gamesToSave =
            when {
                state.pendingGames.isNotEmpty() -> state.pendingGames
                state.selected.size == 6 -> listOf(state.selected.sorted())
                else -> emptyList()
            }
        if (gamesToSave.isEmpty()) {
            _uiState.update { it.copy(error = "저장할 게임이 없습니다. 번호를 선택 후 추가해 주세요.") }
            return
        }

        viewModelScope.launch {
            val targetSignatures = gamesToSave.map { it.sorted() }.toSet()
            val hasDuplicate =
                ticketRepository
                    .observeCurrentRoundTickets()
                    .first()
                    .flatMap { it.games }
                    .any { game -> game.numbers.map { it.value }.sorted() in targetSignatures }
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
                        gamesToSave.mapIndexed { index, numbers ->
                            LottoGame(
                                slot = GameSlot.entries[index],
                                numbers = numbers.map(::LottoNumber),
                                lockedNumbers = numbers.map(::LottoNumber).toSet(),
                                mode = GameMode.MANUAL,
                            )
                        },
                ),
            )
            _uiState.update {
                it.copy(
                    selected = emptyList(),
                    pendingGames = emptyList(),
                    saved = true,
                    savedGameCount = gamesToSave.size,
                    error = null,
                )
            }
        }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false, savedGameCount = 0) }
    }
}

private const val MAX_MANUAL_GAMES = 5
