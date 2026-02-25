package com.weeklylotto.app.domain.service

import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.TicketBundle

data class WeeklyNumbersWidgetSnapshot(
    val roundLabel: String,
    val bundles: List<TicketBundle>,
)

data class ResultSummaryWidgetSnapshot(
    val roundLabel: String,
    val drawResult: DrawResult?,
    val summaryText: String,
)

interface WidgetDataProvider {
    suspend fun loadWeeklyNumbersSnapshot(): WeeklyNumbersWidgetSnapshot

    suspend fun loadResultSummarySnapshot(): ResultSummaryWidgetSnapshot
}
