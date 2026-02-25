package com.weeklylotto.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.ResultEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class StatsPeriod(
    val label: String,
) {
    ALL("전체"),
    RECENT_4_WEEKS("최근 4주"),
    RECENT_8_WEEKS("최근 8주"),
}

data class StatsUiState(
    val totalPurchaseAmount: Long = 0,
    val totalWinAmount: Long = 0,
    val topNumbers: List<LottoNumber> = emptyList(),
    val totalGames: Int = 0,
    val winningGames: Int = 0,
    val selectedPeriod: StatsPeriod = StatsPeriod.ALL,
) {
    val netProfitAmount: Long = totalWinAmount - totalPurchaseAmount
}

class StatsViewModel(
    private val ticketRepository: TicketRepository,
    private val drawRepository: DrawRepository,
    private val resultEvaluator: ResultEvaluator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var latestBundles: List<TicketBundle> = emptyList()
    private var calculateJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            ticketRepository.observeAllTickets().collectLatest { bundles ->
                latestBundles = bundles
                recalculateStats()
            }
        }
    }

    fun setPeriod(period: StatsPeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.update { it.copy(selectedPeriod = period) }
        recalculateStats()
    }

    private fun recalculateStats() {
        calculateJob?.cancel()
        calculateJob =
            viewModelScope.launch {
                val period = _uiState.value.selectedPeriod
                val filteredBundles = applyPeriodFilter(latestBundles, period)

                val games = filteredBundles.flatMap { it.games }
                val purchase = games.size * 1_000L
                val topNumbers =
                    games
                        .flatMap { it.numbers }
                        .groupingBy { it }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .take(6)
                        .map { it.key }

                val drawCache = mutableMapOf<Int, com.weeklylotto.app.domain.model.DrawResult?>()
                var totalWinAmount = 0L
                var winningGames = 0

                filteredBundles.forEach { bundle ->
                    val draw =
                        drawCache.getOrPut(bundle.round.number) {
                            when (val drawResult = drawRepository.fetchByRound(bundle.round)) {
                                is AppResult.Success -> drawResult.value
                                is AppResult.Failure -> null
                            }
                        }

                    if (draw != null) {
                        bundle.games.forEach { game ->
                            val evaluation = resultEvaluator.evaluate(game, draw)
                            totalWinAmount += PrizePolicy.amountFor(evaluation.rank)
                            if (evaluation.rank != DrawRank.NONE) {
                                winningGames += 1
                            }
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        totalPurchaseAmount = purchase,
                        totalWinAmount = totalWinAmount,
                        totalGames = games.size,
                        topNumbers = topNumbers,
                        winningGames = winningGames,
                    )
                }
            }
    }

    private fun applyPeriodFilter(
        bundles: List<TicketBundle>,
        period: StatsPeriod,
    ): List<TicketBundle> {
        return when (period) {
            StatsPeriod.ALL -> bundles
            StatsPeriod.RECENT_4_WEEKS -> {
                val cutoff = Instant.now().minus(28, ChronoUnit.DAYS)
                bundles.filter { it.createdAt >= cutoff }
            }
            StatsPeriod.RECENT_8_WEEKS -> {
                val cutoff = Instant.now().minus(56, ChronoUnit.DAYS)
                bundles.filter { it.createdAt >= cutoff }
            }
        }
    }
}
