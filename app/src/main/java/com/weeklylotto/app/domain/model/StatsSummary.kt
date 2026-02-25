package com.weeklylotto.app.domain.model

data class StatsSummary(
    val totalPurchaseAmount: Long,
    val totalWinAmount: Long,
    val topNumbers: List<LottoNumber>,
)
