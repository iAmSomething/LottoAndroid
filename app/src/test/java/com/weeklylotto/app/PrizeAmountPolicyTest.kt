package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.PrizeAmountPolicy
import org.junit.Test

class PrizeAmountPolicyTest {
    @Test
    fun 등수별_고정추정금액을_반환한다() {
        assertThat(PrizeAmountPolicy.amountFor(DrawRank.NONE)).isEqualTo(0L)
        assertThat(PrizeAmountPolicy.amountFor(DrawRank.FIFTH)).isEqualTo(5_000L)
        assertThat(PrizeAmountPolicy.amountFor(DrawRank.FOURTH)).isEqualTo(50_000L)
        assertThat(PrizeAmountPolicy.amountFor(DrawRank.THIRD)).isEqualTo(1_500_000L)
        assertThat(PrizeAmountPolicy.amountFor(DrawRank.SECOND)).isEqualTo(50_000_000L)
        assertThat(PrizeAmountPolicy.amountFor(DrawRank.FIRST)).isEqualTo(2_000_000_000L)
    }
}
