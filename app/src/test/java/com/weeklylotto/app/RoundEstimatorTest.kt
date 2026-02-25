package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.RoundEstimator
import org.junit.Test
import java.time.LocalDate

class RoundEstimatorTest {
    @Test
    fun `첫 회차 날짜는 1회차다`() {
        val round = RoundEstimator.estimate(LocalDate.of(2002, 12, 7))

        assertThat(round).isEqualTo(1)
    }

    @Test
    fun `정확히 1주 후는 2회차다`() {
        val round = RoundEstimator.estimate(LocalDate.of(2002, 12, 14))

        assertThat(round).isEqualTo(2)
    }

    @Test
    fun `회차 기준일 전 날짜는 최소 1회차로 보정한다`() {
        val round = RoundEstimator.estimate(LocalDate.of(2002, 11, 30))

        assertThat(round).isEqualTo(1)
    }

    @Test
    fun `중간 날짜는 동일 주차 회차를 유지한다`() {
        val round = RoundEstimator.estimate(LocalDate.of(2002, 12, 13))

        assertThat(round).isEqualTo(1)
    }

    @Test
    fun `다음 추첨일은 항상 토요일이다`() {
        val drawDate = RoundEstimator.nextDrawDate(LocalDate.of(2026, 2, 25))

        assertThat(drawDate).isEqualTo(LocalDate.of(2026, 2, 28))
    }

    @Test
    fun `판매 회차는 다음 추첨 회차를 기준으로 계산한다`() {
        val round = RoundEstimator.currentSalesRound(LocalDate.of(2026, 2, 25))

        assertThat(round).isEqualTo(1213)
    }
}
