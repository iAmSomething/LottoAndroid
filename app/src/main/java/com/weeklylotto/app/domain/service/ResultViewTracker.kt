package com.weeklylotto.app.domain.service

interface ResultViewTracker {
    suspend fun loadLastViewedRound(): Int?

    suspend fun loadRecentViewedRounds(limit: Int = 8): List<Int>

    suspend fun markRoundViewed(roundNumber: Int)
}

object NoOpResultViewTracker : ResultViewTracker {
    override suspend fun loadLastViewedRound(): Int? = null

    override suspend fun loadRecentViewedRounds(limit: Int): List<Int> = emptyList()

    override suspend fun markRoundViewed(roundNumber: Int) = Unit
}
