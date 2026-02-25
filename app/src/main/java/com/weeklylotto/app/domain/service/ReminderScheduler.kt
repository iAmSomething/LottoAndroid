package com.weeklylotto.app.domain.service

import com.weeklylotto.app.domain.model.ReminderConfig

interface ReminderScheduler {
    suspend fun schedule(config: ReminderConfig)

    suspend fun cancelAll()
}
