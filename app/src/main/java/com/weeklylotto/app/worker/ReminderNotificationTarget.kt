package com.weeklylotto.app.worker

import com.weeklylotto.app.ui.navigation.AppDestination

enum class ReminderNotificationTarget(
    val key: String,
    val route: String,
    val snoozeWorkName: String,
) {
    PURCHASE(
        key = "purchase",
        route = AppDestination.QrScan.route,
        snoozeWorkName = "purchase_reminder_snooze_work",
    ),
    RESULT(
        key = "result",
        route = AppDestination.Result.route,
        snoozeWorkName = "result_reminder_snooze_work",
    ),
    ;

    companion object {
        fun fromKey(key: String?): ReminderNotificationTarget? =
            entries.firstOrNull { it.key == key }
    }
}
