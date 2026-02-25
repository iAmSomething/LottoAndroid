package com.weeklylotto.app.domain.model

import java.time.Instant

data class TicketBundle(
    val id: Long = 0,
    val round: Round,
    val games: List<LottoGame>,
    val source: TicketSource,
    val createdAt: Instant = Instant.now(),
    val status: TicketStatus = TicketStatus.PENDING,
)

enum class TicketSource {
    GENERATED,
    QR_SCAN,
    MANUAL,
}
