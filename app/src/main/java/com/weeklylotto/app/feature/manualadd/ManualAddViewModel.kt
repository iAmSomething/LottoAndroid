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
    val lastSavedGameCount: Int = 0,
    val lastSavedTicketId: Long? = null,
    val error: String? = null,
    val duplicatePrompt: DuplicatePromptUi? = null,
)

data class DuplicatePromptUi(
    val totalGameCount: Int,
    val duplicateGameCount: Int,
)

class ManualAddViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()
    private var queuedGamesForDuplicateDecision: List<List<Int>> = emptyList()

    fun toggleNumber(number: Int) {
        _uiState.update { state ->
            val selected = state.selected.toMutableList()
            when {
                selected.contains(number) -> selected.remove(number)
                selected.size < 6 -> selected.add(number)
            }
            state.copy(selected = selected.sorted(), error = null, duplicatePrompt = null)
        }
    }

    fun clear() {
        _uiState.update { it.copy(selected = emptyList(), error = null, duplicatePrompt = null) }
    }

    fun clearPendingGames() {
        _uiState.update { it.copy(pendingGames = emptyList(), error = null, duplicatePrompt = null) }
    }

    fun setRepeatCount(value: Int) {
        _uiState.update { it.copy(repeatCount = value.coerceIn(1, 5), error = null, duplicatePrompt = null) }
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
                duplicatePrompt = null,
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
                duplicatePrompt = null,
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
                duplicatePrompt = null,
            )
        }
    }

    fun autoFill() {
        _uiState.update { state ->
            if (state.selected.size >= 6) return@update state
            val remaining = (1..45).filterNot { it in state.selected }.shuffled().take(6 - state.selected.size)
            state.copy(selected = (state.selected + remaining).sorted(), error = null, duplicatePrompt = null)
        }
    }

    fun save() {
        val state = uiState.value
        val gamesToSave =
            when {
                state.pendingGames.isNotEmpty() -> state.pendingGames
                state.selected.size == 6 -> listOf(state.selected.sorted())
                else -> emptyList()
            }.map { numbers -> numbers.sorted() }
        if (gamesToSave.isEmpty()) {
            _uiState.update { it.copy(error = "저장할 게임이 없습니다. 번호를 선택 후 추가해 주세요.") }
            return
        }
        queuedGamesForDuplicateDecision = emptyList()

        viewModelScope.launch {
            val existingSignatures = currentRoundSignatures()
            val duplicateGameCount = gamesToSave.count { signature -> signature in existingSignatures }
            if (duplicateGameCount > 0) {
                queuedGamesForDuplicateDecision = gamesToSave
                _uiState.update {
                    it.copy(
                        saved = false,
                        error = null,
                        duplicatePrompt =
                            DuplicatePromptUi(
                                totalGameCount = gamesToSave.size,
                                duplicateGameCount = duplicateGameCount,
                            ),
                    )
                }
                return@launch
            }
            persistGames(gamesToSave)
        }
    }

    fun cancelDuplicateSave() {
        queuedGamesForDuplicateDecision = emptyList()
        _uiState.update {
            it.copy(
                duplicatePrompt = null,
                error = "저장을 취소했습니다.",
            )
        }
    }

    fun saveExcludingDuplicates() {
        val queued = queuedGamesForDuplicateDecision
        if (queued.isEmpty()) return
        viewModelScope.launch {
            val existingSignatures = currentRoundSignatures()
            val filtered = queued.filterNot { signature -> signature in existingSignatures }
            if (filtered.isEmpty()) {
                queuedGamesForDuplicateDecision = emptyList()
                _uiState.update {
                    it.copy(
                        duplicatePrompt = null,
                        error = "중복 제외 시 저장할 게임이 없습니다.",
                    )
                }
                return@launch
            }
            persistGames(filtered)
        }
    }

    fun saveIncludingDuplicates() {
        val queued = queuedGamesForDuplicateDecision
        if (queued.isEmpty()) return
        viewModelScope.launch {
            persistGames(queued)
        }
    }

    fun undoLastSavedTicket() {
        val targetId = uiState.value.lastSavedTicketId ?: return
        viewModelScope.launch {
            val deleted = runCatching { ticketRepository.deleteByIds(setOf(targetId)) }
            if (deleted.isSuccess) {
                _uiState.update {
                    it.copy(
                        lastSavedTicketId = null,
                        lastSavedGameCount = 0,
                        error = "직전 저장을 취소했습니다.",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(error = "저장 취소에 실패했습니다. 다시 시도해 주세요.")
                }
            }
        }
    }

    fun dismissLastSavedAction() {
        _uiState.update {
            it.copy(
                lastSavedTicketId = null,
                lastSavedGameCount = 0,
            )
        }
    }

    private suspend fun currentRoundSignatures(): Set<List<Int>> =
        ticketRepository
            .observeCurrentRoundTickets()
            .first()
            .flatMap { it.games }
            .map { game -> game.numbers.map { it.value }.sorted() }
            .toSet()

    private suspend fun persistGames(gamesToSave: List<List<Int>>) {
        val today = LocalDate.now()
        val drawDate = RoundEstimator.nextDrawDate(today)
        val bundleToSave =
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
            )
        val saved = runCatching { ticketRepository.save(bundleToSave) }
        if (saved.isFailure) {
            _uiState.update {
                it.copy(
                    saved = false,
                    duplicatePrompt = null,
                    error = "저장에 실패했습니다. 다시 시도해 주세요.",
                )
            }
            return
        }
        val savedTicketId =
            runCatching { ticketRepository.latest()?.id }
                .getOrNull()
                ?.takeIf { id -> id > 0L }
        queuedGamesForDuplicateDecision = emptyList()
        _uiState.update {
            it.copy(
                selected = emptyList(),
                pendingGames = emptyList(),
                saved = true,
                savedGameCount = gamesToSave.size,
                lastSavedGameCount = gamesToSave.size,
                lastSavedTicketId = savedTicketId,
                error = null,
                duplicatePrompt = null,
            )
        }
    }

    fun consumeSaved() {
        _uiState.update {
            it.copy(
                saved = false,
                savedGameCount = 0,
                duplicatePrompt = null,
            )
        }
    }
}

private const val MAX_MANUAL_GAMES = 5
