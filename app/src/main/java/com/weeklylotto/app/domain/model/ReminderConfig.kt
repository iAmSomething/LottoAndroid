package com.weeklylotto.app.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class ReminderConfig(
    val purchaseReminderDay: DayOfWeek = DayOfWeek.SATURDAY,
    val purchaseReminderTime: LocalTime = LocalTime.of(15, 0),
    val resultReminderDay: DayOfWeek = DayOfWeek.SATURDAY,
    val resultReminderTime: LocalTime = LocalTime.of(21, 0),
    val enabled: Boolean = true,
)
