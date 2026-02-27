package com.weeklylotto.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.PrizeAmountPolicy
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class StatsPeriod(
    val label: String,
) {
    ALL("전체"),
    RECENT_4_WEEKS("최근 4주"),
    RECENT_8_WEEKS("최근 8주"),
    CUSTOM("직접 입력"),
}

data class StatsUiState(
    val totalPurchaseAmount: Long = 0,
    val totalWinAmount: Long = 0,
    val topNumbers: List<LottoNumber> = emptyList(),
    val totalGames: Int = 0,
    val winningGames: Int = 0,
    val selectedPeriod: StatsPeriod = StatsPeriod.ALL,
    val customStartRound: String = "",
    val customEndRound: String = "",
    val customRangeError: String? = null,
    val sourceStats: List<SourceStats> = TicketSource.entries.map { SourceStats(source = it) },
    val roiTrend: List<RoiTrendPoint> = emptyList(),
    val numberDistribution: List<NumberRangeBucket> = defaultNumberRangeBuckets(),
) {
    val netProfitAmount: Long = totalWinAmount - totalPurchaseAmount
}

data class NumberRangeBucket(
    val label: String,
    val start: Int,
    val end: Int,
    val count: Int = 0,
    val percent: Int = 0,
)

data class SourceStats(
    val source: TicketSource,
    val totalGames: Int = 0,
    val winningGames: Int = 0,
    val totalPurchaseAmount: Long = 0,
    val totalWinAmount: Long = 0,
) {
    val netProfitAmount: Long = totalWinAmount - totalPurchaseAmount

    val winRatePercent: Int =
        if (totalGames == 0) {
            0
        } else {
            (winningGames * 100) / totalGames
        }

    val roiPercent: Int =
        if (totalPurchaseAmount == 0L) {
            0
        } else {
            ((netProfitAmount * 100) / totalPurchaseAmount).toInt()
        }
}

data class RoiTrendPoint(
    val round: Int,
    val drawDate: LocalDate,
    val totalGames: Int = 0,
    val winningGames: Int = 0,
    val totalPurchaseAmount: Long = 0,
    val totalWinAmount: Long = 0,
) {
    val netProfitAmount: Long = totalWinAmount - totalPurchaseAmount

    val roiPercent: Int =
        if (totalPurchaseAmount == 0L) {
            0
        } else {
            ((netProfitAmount * 100) / totalPurchaseAmount).toInt()
        }
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
        _uiState.update { it.copy(selectedPeriod = period, customRangeError = null) }
        recalculateStats()
    }

    fun updateCustomRoundRange(
        startRound: String,
        endRound: String,
    ) {
        _uiState.update {
            it.copy(
                customStartRound = startRound.filter(Char::isDigit).take(4),
                customEndRound = endRound.filter(Char::isDigit).take(4),
                customRangeError = null,
            )
        }
    }

    fun applyCustomRoundRange() {
        val startRound = _uiState.value.customStartRound.toIntOrNull()
        val endRound = _uiState.value.customEndRound.toIntOrNull()
        val error =
            when {
                _uiState.value.customStartRound.isBlank() || _uiState.value.customEndRound.isBlank() ->
                    "시작/끝 회차를 모두 입력하세요."
                startRound == null || endRound == null -> "회차는 숫자만 입력하세요."
                startRound <= 0 || endRound <= 0 -> "회차는 1 이상이어야 합니다."
                startRound > endRound -> "시작 회차는 끝 회차보다 클 수 없습니다."
                else -> null
            }
        if (error != null) {
            _uiState.update { it.copy(customRangeError = error) }
            return
        }
        _uiState.update { it.copy(selectedPeriod = StatsPeriod.CUSTOM, customRangeError = null) }
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
                val pickedNumbers = games.flatMap { game -> game.numbers.map { number -> number.value } }
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
                val sourceStatsAccumulator =
                    TicketSource.entries.associateWith { MutableSourceStats() }.toMutableMap()
                val roundRoiAccumulator = mutableMapOf<Int, MutableRoundRoiStats>()

                filteredBundles.forEach { bundle ->
                    val sourceAccumulator = sourceStatsAccumulator.getValue(bundle.source)
                    sourceAccumulator.totalGames += bundle.games.size
                    sourceAccumulator.totalPurchaseAmount += bundle.games.size * 1_000L

                    val roundAccumulator =
                        roundRoiAccumulator.getOrPut(bundle.round.number) {
                            MutableRoundRoiStats(drawDate = bundle.round.drawDate)
                        }
                    roundAccumulator.totalGames += bundle.games.size
                    roundAccumulator.totalPurchaseAmount += bundle.games.size * 1_000L

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
                            val prizeAmount = PrizeAmountPolicy.amountFor(evaluation.rank)
                            totalWinAmount += prizeAmount
                            sourceAccumulator.totalWinAmount += prizeAmount
                            roundAccumulator.totalWinAmount += prizeAmount
                            if (evaluation.rank != DrawRank.NONE) {
                                winningGames += 1
                                sourceAccumulator.winningGames += 1
                                roundAccumulator.winningGames += 1
                            }
                        }
                    }
                }

                val sourceStats =
                    TicketSource.entries.map { source ->
                        val value = sourceStatsAccumulator.getValue(source)
                        SourceStats(
                            source = source,
                            totalGames = value.totalGames,
                            winningGames = value.winningGames,
                            totalPurchaseAmount = value.totalPurchaseAmount,
                            totalWinAmount = value.totalWinAmount,
                        )
                    }

                val roiTrend =
                    roundRoiAccumulator
                        .entries
                        .sortedBy { it.key }
                        .takeLast(8)
                        .map { (round, value) ->
                            RoiTrendPoint(
                                round = round,
                                drawDate = value.drawDate,
                                totalGames = value.totalGames,
                                winningGames = value.winningGames,
                                totalPurchaseAmount = value.totalPurchaseAmount,
                                totalWinAmount = value.totalWinAmount,
                            )
                        }

                val totalPickedNumbers = pickedNumbers.size
                val numberDistribution =
                    defaultNumberRangeBuckets().map { bucket ->
                        val count = pickedNumbers.count { number -> number in bucket.start..bucket.end }
                        NumberRangeBucket(
                            label = bucket.label,
                            start = bucket.start,
                            end = bucket.end,
                            count = count,
                            percent =
                                if (totalPickedNumbers == 0) {
                                    0
                                } else {
                                    (count * 100) / totalPickedNumbers
                                },
                        )
                    }

                _uiState.update {
                    it.copy(
                        totalPurchaseAmount = purchase,
                        totalWinAmount = totalWinAmount,
                        totalGames = games.size,
                        topNumbers = topNumbers,
                        winningGames = winningGames,
                        sourceStats = sourceStats,
                        roiTrend = roiTrend,
                        numberDistribution = numberDistribution,
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
            StatsPeriod.CUSTOM -> {
                val startRound = _uiState.value.customStartRound.toIntOrNull() ?: return emptyList()
                val endRound = _uiState.value.customEndRound.toIntOrNull() ?: return emptyList()
                bundles.filter { it.round.number in startRound..endRound }
            }
        }
    }
}

private data class MutableSourceStats(
    var totalGames: Int = 0,
    var winningGames: Int = 0,
    var totalPurchaseAmount: Long = 0,
    var totalWinAmount: Long = 0,
)

private fun defaultNumberRangeBuckets(): List<NumberRangeBucket> =
    listOf(
        NumberRangeBucket(label = "1-9", start = 1, end = 9),
        NumberRangeBucket(label = "10-19", start = 10, end = 19),
        NumberRangeBucket(label = "20-29", start = 20, end = 29),
        NumberRangeBucket(label = "30-39", start = 30, end = 39),
        NumberRangeBucket(label = "40-45", start = 40, end = 45),
    )

private data class MutableRoundRoiStats(
    val drawDate: LocalDate,
    var totalGames: Int = 0,
    var winningGames: Int = 0,
    var totalPurchaseAmount: Long = 0,
    var totalWinAmount: Long = 0,
)
