package com.weeklylotto.app.data.analytics

import android.util.Log
import com.weeklylotto.app.domain.service.AnalyticsLogger

class LogcatAnalyticsLogger(
    private val tag: String = DEFAULT_TAG,
) : AnalyticsLogger {
    override fun log(
        event: String,
        params: Map<String, String>,
    ) {
        val payload =
            if (params.isEmpty()) {
                event
            } else {
                "$event | " + params.entries.joinToString(separator = ",") { "${it.key}=${it.value}" }
            }
        Log.i(tag, payload)
    }

    private companion object {
        const val DEFAULT_TAG = "WeeklyLottoAnalytics"
    }
}
