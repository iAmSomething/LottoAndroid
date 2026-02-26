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
    const val MOTION_SPLASH_SHOWN = "motion_splash_shown"
    const val MOTION_SPLASH_SKIP = "motion_splash_skip"
}

object AnalyticsParamKey {
    const val SCREEN = "screen"
    const val COMPONENT = "component"
    const val ACTION = "action"
}

object AnalyticsActionValue {
    const val CLICK = "click"
    const val APPLY = "apply"
    const val LOCK = "lock"
    const val UNLOCK = "unlock"
    const val COLD = "cold"
    const val WARM = "warm"
    const val COMPACT = "compact"
}
