package com.weeklylotto.app.feature.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.EvaluationResult
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.ResultEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EvaluatedGameUi(
    val game: LottoGame,
    val result: EvaluationResult,
)

data class ResultUiState(
    val loading: Boolean = false,
    val error: ResultErrorUi? = null,
    val drawResult: DrawResult? = null,
    val evaluatedGames: List<EvaluatedGameUi> = emptyList(),
    val selectedRound: Int? = null,
    val availableRounds: List<Int> = emptyList(),
) {
    val winningCount: Int get() = evaluatedGames.count { it.result.rank != DrawRank.NONE }
}

class ResultViewModel(
    private val drawRepository: DrawRepository,
    private val ticketRepository: TicketRepository,
    private val evaluator: ResultEvaluator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResultUiState(loading = true))
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val selectedRound = uiState.value.selectedRound
        if (selectedRound != null) {
            loadByRound(selectedRound)
            return
        }
        loadLatest()
    }

    fun selectRound(round: Int) {
        if (round == uiState.value.selectedRound) return
        loadByRound(round)
    }

    private fun loadLatest() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            when (val latestDraw = drawRepository.fetchLatest()) {
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = latestDraw.error.toResultErrorUi(),
                        )
                    }
                }

                is AppResult.Success -> {
                    updateWithDraw(latestDraw.value)
                }
            }
        }
    }

    private fun loadByRound(roundNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val round = Round(number = roundNumber, drawDate = LocalDate.now())
            when (val draw = drawRepository.fetchByRound(round)) {
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = draw.error.toResultErrorUi(),
                        )
                    }
                }

                is AppResult.Success -> {
                    updateWithDraw(draw.value)
                }
            }
        }
    }

    private suspend fun updateWithDraw(draw: DrawResult) {
        val bundles = ticketRepository.observeTicketsByRound(draw.round).first()
        val evaluated =
            bundles.flatMap { bundle ->
                bundle.games.map { game ->
                    EvaluatedGameUi(
                        game = game,
                        result = evaluator.evaluate(game, draw),
                    )
                }
            }

        _uiState.update { state ->
            val availableRounds =
                if (state.availableRounds.isNotEmpty()) {
                    (state.availableRounds + draw.round.number).distinct().sortedDescending()
                } else {
                    (draw.round.number downTo (draw.round.number - 15).coerceAtLeast(1)).toList()
                }

            state.copy(
                loading = false,
                drawResult = draw,
                evaluatedGames = evaluated,
                selectedRound = draw.round.number,
                availableRounds = availableRounds,
            )
        }
    }
}
