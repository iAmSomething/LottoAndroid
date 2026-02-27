package com.weeklylotto.app.feature.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.EvaluationResult
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.PrizeAmountPolicy
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.NoOpResultViewTracker
import com.weeklylotto.app.domain.service.ResultEvaluator
import com.weeklylotto.app.domain.service.ResultViewTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

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
    val retryAttempt: Int = 0,
    val maxRetryAttempt: Int = 3,
    val lastErrorAt: LocalDateTime? = null,
) {
    val winningCount: Int get() = evaluatedGames.count { it.result.rank != DrawRank.NONE }
    val totalWinningAmount: Long get() = evaluatedGames.sumOf { PrizeAmountPolicy.amountFor(it.result.rank) }
    val hasRetried: Boolean get() = retryAttempt > 1
}

class ResultViewModel(
    private val drawRepository: DrawRepository,
    private val ticketRepository: TicketRepository,
    private val evaluator: ResultEvaluator,
    private val resultViewTracker: ResultViewTracker = NoOpResultViewTracker,
    private val retryDelayProvider: (Int) -> Long = { attempt -> attempt * 1_000L },
    private val nowProvider: () -> LocalDateTime = LocalDateTime::now,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResultUiState(loading = true))
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        restoreLastViewedRoundAndRefresh()
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

    fun loadLatestFromError() {
        loadLatest()
    }

    private fun restoreLastViewedRoundAndRefresh() {
        viewModelScope.launch {
            val lastViewedRound = resultViewTracker.loadLastViewedRound()
            if (lastViewedRound != null) {
                loadByRound(lastViewedRound)
            } else {
                loadLatest()
            }
        }
    }

    private fun loadLatest() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, retryAttempt = 0) }

            when (val latestDraw = fetchWithRetry { drawRepository.fetchLatest() }) {
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = latestDraw.error.toResultErrorUi(),
                            lastErrorAt = nowProvider(),
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
            _uiState.update {
                it.copy(
                    loading = true,
                    error = null,
                    retryAttempt = 0,
                    selectedRound = roundNumber,
                )
            }
            val round = Round(number = roundNumber, drawDate = LocalDate.now())
            when (val draw = fetchWithRetry { drawRepository.fetchByRound(round) }) {
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = draw.error.toResultErrorUi(),
                            lastErrorAt = nowProvider(),
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
        resultViewTracker.markRoundViewed(draw.round.number)
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
                retryAttempt = 0,
                lastErrorAt = null,
            )
        }
    }

    private suspend fun fetchWithRetry(request: suspend () -> AppResult<DrawResult>): AppResult<DrawResult> {
        val maxAttempt = uiState.value.maxRetryAttempt
        var lastFailure: AppResult.Failure? = null

        for (attempt in 1..maxAttempt) {
            _uiState.update { it.copy(retryAttempt = attempt) }

            when (val result = request()) {
                is AppResult.Success -> return AppResult.Success(result.value)
                is AppResult.Failure -> {
                    lastFailure = result
                    val shouldRetry = attempt < maxAttempt && result.error.isRetriable()
                    if (shouldRetry) {
                        delay(retryDelayProvider(attempt))
                    } else {
                        break
                    }
                }
            }
        }
        return lastFailure ?: AppResult.Failure(AppError.NetworkError(message = "당첨 결과를 불러오지 못했습니다."))
    }
}

private fun AppError.isRetriable(): Boolean =
    when (this) {
        is AppError.NetworkError -> true
        else -> false
    }
