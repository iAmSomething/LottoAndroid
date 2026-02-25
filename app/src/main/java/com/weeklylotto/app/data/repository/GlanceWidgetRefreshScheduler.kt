package com.weeklylotto.app.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler
import com.weeklylotto.app.widget.ResultSummaryWidget
import com.weeklylotto.app.widget.WeeklyNumbersWidget

class GlanceWidgetRefreshScheduler(
    private val context: Context,
) : WidgetRefreshScheduler {
    private val logger = WidgetRefreshHistoryLogger(context)

    override suspend fun refreshAll() {
        logger.log("refresh_all:start")
        runWidgetRefresh("weekly_numbers") {
            WeeklyNumbersWidget().updateAll(context)
        }
        runWidgetRefresh("result_summary") {
            ResultSummaryWidget().updateAll(context)
        }
        logger.log("refresh_all:done")
    }

    private suspend fun runWidgetRefresh(
        widgetName: String,
        action: suspend () -> Unit,
    ) {
        runCatching { action() }
            .onSuccess { logger.log("$widgetName:success") }
            .onFailure { error ->
                logger.log("$widgetName:failure:${error.javaClass.simpleName}:${error.message.orEmpty().take(120)}")
            }
    }
}
