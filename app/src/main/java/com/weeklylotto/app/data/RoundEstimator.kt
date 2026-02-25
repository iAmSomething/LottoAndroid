package com.weeklylotto.app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RoundEstimator {
    private val firstRoundDate: LocalDate = LocalDate.of(2002, 12, 7)

    fun estimate(date: LocalDate = LocalDate.now()): Int {
        val weeks = ChronoUnit.WEEKS.between(firstRoundDate, date).toInt()
        return (weeks + 1).coerceAtLeast(1)
    }
}
