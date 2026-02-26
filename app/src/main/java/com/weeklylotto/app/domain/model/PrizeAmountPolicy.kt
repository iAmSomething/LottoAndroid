package com.weeklylotto.app.domain.model

object PrizeAmountPolicy {
    fun amountFor(rank: DrawRank): Long =
        when (rank) {
            DrawRank.NONE -> 0L
            DrawRank.FIFTH -> 5_000L
            DrawRank.FOURTH -> 50_000L
            DrawRank.THIRD -> 1_500_000L
            DrawRank.SECOND -> 50_000_000L
            DrawRank.FIRST -> 2_000_000_000L
        }
}
