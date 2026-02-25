package com.weeklylotto.app.domain.repository

import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.Round

interface DrawRepository {
    suspend fun fetchLatest(): AppResult<DrawResult>

    suspend fun fetchByRound(round: Round): AppResult<DrawResult>
}
