package com.weeklylotto.app.domain.service

interface PurchaseRedirectNoticeStore {
    suspend fun hasSeenNotice(): Boolean

    suspend fun markNoticeSeen()
}

object NoOpPurchaseRedirectNoticeStore : PurchaseRedirectNoticeStore {
    override suspend fun hasSeenNotice(): Boolean = true

    override suspend fun markNoticeSeen() = Unit
}
