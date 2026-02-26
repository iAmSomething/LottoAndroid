package com.weeklylotto.app.domain.service

interface AnalyticsLogger {
    fun log(
        event: String,
        params: Map<String, String> = emptyMap(),
    )
}

object NoOpAnalyticsLogger : AnalyticsLogger {
    override fun log(
        event: String,
        params: Map<String, String>,
    ) = Unit
}

object AnalyticsEvent {
    const val INTERACTION_CTA_PRESS = "interaction_cta_press"
    const val INTERACTION_BALL_LOCK_TOGGLE = "interaction_ball_lock_toggle"
    const val INTERACTION_SHEET_APPLY = "interaction_sheet_apply"
}

object AnalyticsParamKey {
    const val SCREEN = "screen"
    const val COMPONENT = "component"
    const val ACTION = "action"
}
