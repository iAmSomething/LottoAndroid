package com.weeklylotto.app.domain.service

import kotlinx.coroutines.flow.Flow

interface MotionPreferenceStore {
    fun observeReduceMotionEnabled(): Flow<Boolean>

    suspend fun loadReduceMotionEnabled(): Boolean

    suspend fun saveReduceMotionEnabled(enabled: Boolean)
}
