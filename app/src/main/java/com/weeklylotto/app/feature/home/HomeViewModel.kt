package com.weeklylotto.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.PrizeAmountPolicy
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.ResultEvaluator
import com.weeklylotto.app.domain.service.ResultViewTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeeklyReportSummary(
    val round: Int,
    val totalGames: Int,
    val winningGames: Int,
    val totalPurchaseAmount: Long,
    val totalWinningAmount: Long,
) {
    val netProfitAmount: Long get() = totalWinningAmount - totalPurchaseAmount
}

data class HomeUiState(
    val currentRound: Int,
    val drawDate: LocalDate,
    val dDay: Int,
    val bundles: List<TicketBundle> = emptyList(),
    val hasUnseenResult: Boolean = false,
    val unseenRound: Int? = null,
    val weeklyReport: WeeklyReportSummary? = null,
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
    private val drawRepository: DrawRepository,
    private val resultEvaluator: ResultEvaluator,
    private val resultViewTracker: ResultViewTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState.initial())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allTickets: List<TicketBundle> = emptyList()
    private var latestDrawRound: Int? = null
    private var latestDraw: DrawResult? = null
    private var lastViewedRound: Int? = null

    init {
        viewModelScope.launch {
            ticketRepository.observeCurrentRoundTickets().collect { bundles ->
                _uiState.update { state -> state.copy(bundles = bundles) }
            }
        }
        viewModelScope.launch {
            ticketRepository.observeAllTickets().collect { bundles ->
                allTickets = bundles
                recalculateResultInsights()
            }
        }
        viewModelScope.launch {
            lastViewedRound = resultViewTracker.loadLastViewedRound()
            recalculateResultInsights()
        }
        viewModelScope.launch {
            latestDraw = loadLatestDraw()
            latestDrawRound = latestDraw?.round?.number
            recalculateResultInsights()
        }
    }

    private suspend fun loadLatestDraw(): DrawResult? =
        when (val result = drawRepository.fetchLatest()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> null
        }

    private suspend fun recalculateResultInsights() {
        val drawRound = latestDrawRound ?: return
        val draw = latestDraw

        val ticketsForDraw = allTickets.filter { it.round.number == drawRound }
        val totalGames = ticketsForDraw.sumOf { it.games.size }
        val winningGames =
            if (draw == null) {
                0
            } else {
                ticketsForDraw.sumOf { bundle ->
                    bundle.games.count { game ->
                        resultEvaluator.evaluate(game, draw).rank != DrawRank.NONE
                    }
                }
            }
        val totalWinningAmount =
            if (draw == null) {
                0L
            } else {
                ticketsForDraw.sumOf { bundle ->
                    bundle.games.sumOf { game ->
                        PrizeAmountPolicy.amountFor(resultEvaluator.evaluate(game, draw).rank)
                    }
                }
            }
        val report =
            WeeklyReportSummary(
                round = drawRound,
                totalGames = totalGames,
                winningGames = winningGames,
                totalPurchaseAmount = totalGames * 1_000L,
                totalWinningAmount = totalWinningAmount,
            )

        val viewedRound = lastViewedRound ?: 0
        val hasUnseenResult = totalGames > 0 && drawRound > viewedRound

        _uiState.update { state ->
            state.copy(
                hasUnseenResult = hasUnseenResult,
                unseenRound = if (hasUnseenResult) drawRound else null,
                weeklyReport = report,
            )
        }
    }
}
