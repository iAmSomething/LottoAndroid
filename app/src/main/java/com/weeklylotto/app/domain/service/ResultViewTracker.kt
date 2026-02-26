package com.weeklylotto.app.domain.service

interface ResultViewTracker {
    suspend fun loadLastViewedRound(): Int?

    suspend fun markRoundViewed(roundNumber: Int)
}

object NoOpResultViewTracker : ResultViewTracker {
    override suspend fun loadLastViewedRound(): Int? = null

    override suspend fun markRoundViewed(roundNumber: Int) = Unit
}
