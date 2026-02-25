package com.weeklylotto.app.domain.service

interface WidgetRefreshScheduler {
    suspend fun refreshAll()
}
