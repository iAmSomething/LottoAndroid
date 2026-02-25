package com.weeklylotto.app.domain.service

import com.weeklylotto.app.domain.model.ReminderConfig

interface ReminderConfigStore {
    suspend fun load(): ReminderConfig

    suspend fun save(config: ReminderConfig)
}
