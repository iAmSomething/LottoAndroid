package com.weeklylotto.app.worker

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

data class PurchaseReminderMessage(
    val shouldNotify: Boolean,
    val title: String,
    val body: String,
)

private val PURCHASE_GOAL_CUTOFF_TIME: LocalTime = LocalTime.of(18, 0)

fun resolvePurchaseReminderMessage(
    now: LocalDateTime,
    hasCurrentRoundTickets: Boolean,
): PurchaseReminderMessage {
    val isSaturdayBeforeCutoff =
        now.dayOfWeek == DayOfWeek.SATURDAY && now.toLocalTime().isBefore(PURCHASE_GOAL_CUTOFF_TIME)
    if (!isSaturdayBeforeCutoff) {
        return PurchaseReminderMessage(
            shouldNotify = true,
            title = "이번 주 로또 구매 시간",
            body = "구매하실 번호를 확인하고 QR로 등록해보세요.",
        )
    }

    if (hasCurrentRoundTickets) {
        return PurchaseReminderMessage(
            shouldNotify = false,
            title = "",
            body = "",
        )
    }

    return PurchaseReminderMessage(
        shouldNotify = true,
        title = "오늘 18시 전 번호 등록 목표",
        body = "아직 이번 주 번호가 없어요. 지금 등록하고 주간 루틴을 지켜보세요.",
    )
}
