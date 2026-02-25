package com.weeklylotto.app.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RoundEstimator {
    private val firstRoundDate: LocalDate = LocalDate.of(2002, 12, 7)

    fun estimate(date: LocalDate = LocalDate.now()): Int {
        val weeks = ChronoUnit.WEEKS.between(firstRoundDate, date).toInt()
        return (weeks + 1).coerceAtLeast(1)
    }

    fun nextDrawDate(from: LocalDate = LocalDate.now()): LocalDate {
        var candidate = from
        while (candidate.dayOfWeek != DayOfWeek.SATURDAY) {
            candidate = candidate.plusDays(1)
        }
        return candidate
    }

    fun currentSalesRound(date: LocalDate = LocalDate.now()): Int = estimate(nextDrawDate(date))
}
