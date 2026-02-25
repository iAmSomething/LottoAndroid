package com.weeklylotto.app.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler
import com.weeklylotto.app.widget.ResultSummaryWidget
import com.weeklylotto.app.widget.WeeklyNumbersWidget

class GlanceWidgetRefreshScheduler(
    private val context: Context,
) : WidgetRefreshScheduler {
    override suspend fun refreshAll() {
        WeeklyNumbersWidget().updateAll(context)
        ResultSummaryWidget().updateAll(context)
    }
}
