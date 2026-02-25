package com.weeklylotto.app.domain.model

data class ParsedTicket(
    val round: Int,
    val games: List<List<LottoNumber>>,
)
