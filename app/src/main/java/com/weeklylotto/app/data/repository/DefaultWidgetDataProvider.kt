package com.weeklylotto.app.data.repository

import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.ResultEvaluator
import com.weeklylotto.app.domain.service.ResultSummaryWidgetSnapshot
import com.weeklylotto.app.domain.service.WeeklyNumbersWidgetSnapshot
import com.weeklylotto.app.domain.service.WidgetDataProvider
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DefaultWidgetDataProvider(
    private val ticketRepository: TicketRepository,
    private val drawRepository: DrawRepository,
    private val resultEvaluator: ResultEvaluator,
) : WidgetDataProvider {
    override suspend fun loadWeeklyNumbersSnapshot(): WeeklyNumbersWidgetSnapshot {
        val bundles = ticketRepository.observeCurrentRoundTickets().first()
        val roundLabel = bundles.firstOrNull()?.round?.number?.let { "이번주 ${it}회" } ?: "이번주 번호 없음"
        return WeeklyNumbersWidgetSnapshot(
            roundLabel = roundLabel,
            bundles = bundles,
        )
    }

    override suspend fun loadResultSummarySnapshot(): ResultSummaryWidgetSnapshot {
        val latestDraw =
            when (val result = drawRepository.fetchLatest()) {
                is com.weeklylotto.app.domain.error.AppResult.Success -> result.value
                is com.weeklylotto.app.domain.error.AppResult.Failure -> null
            }

        val matchingBundles =
            ticketRepository.observeTicketsByRound(
                round = latestDraw?.round ?: Round(0, LocalDate.now()),
            ).first()

        val bestRank =
            matchingBundles
                .flatMap {
                        bundle ->
                    bundle.games.mapNotNull {
                            game ->
                        latestDraw?.let { resultEvaluator.evaluate(game, it).rank }
                    }
                }
                .maxByOrNull { it.ordinal }
                ?: DrawRank.NONE

        val summary = if (bestRank == DrawRank.NONE) "아쉽지만 다음 기회에" else "${bestRank.label} 당첨!"

        return ResultSummaryWidgetSnapshot(
            roundLabel = latestDraw?.round?.number?.let { "${it}회 결과" } ?: "결과 없음",
            drawResult = latestDraw,
            summaryText = summary,
        )
    }
}
