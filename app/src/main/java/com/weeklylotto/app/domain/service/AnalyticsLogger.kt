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
    const val OPS_API_REQUEST = "ops_api_request"
    const val OPS_STORAGE_MUTATION = "ops_storage_mutation"
    const val OPS_DATA_INTEGRITY = "ops_data_integrity"
}

object AnalyticsParamKey {
    const val SCREEN = "screen"
    const val COMPONENT = "component"
    const val ACTION = "action"
    const val STATUS = "status"
    const val SOURCE = "source"
    const val OPERATION = "operation"
    const val LATENCY_MS = "latency_ms"
    const val ERROR_TYPE = "error_type"
    const val ROUND = "round"
    const val ISSUE_COUNT = "issue_count"
}

object AnalyticsActionValue {
    const val CLICK = "click"
    const val APPLY = "apply"
    const val LOCK = "lock"
    const val UNLOCK = "unlock"
    const val COLD = "cold"
    const val WARM = "warm"
    const val COMPACT = "compact"
    const val PURCHASE_REDIRECT_TAP = "purchase_redirect_tap"
    const val PURCHASE_REDIRECT_CONFIRM = "purchase_redirect_confirm"
    const val PURCHASE_REDIRECT_FAIL = "purchase_redirect_fail"
    const val PURCHASE_REDIRECT_OPEN_BROWSER = "purchase_redirect_open_browser"
    const val PURCHASE_REDIRECT_COPY_LINK = "purchase_redirect_copy_link"
    const val LOCATION_STORE_SEARCH_TAP = "location_store_search_tap"
    const val LOCATION_STORE_SEARCH_OPEN_MAP = "location_store_search_open_map"
    const val LOCATION_STORE_SEARCH_FAIL = "location_store_search_fail"
    const val LOCATION_STORE_SEARCH_COPY_LINK = "location_store_search_copy_link"
    const val DUPLICATE_WARNING_GENERATE = "duplicate_warning_generate"
    const val PREFERRED_PATTERN_GENERATE = "preferred_pattern_generate"
}
