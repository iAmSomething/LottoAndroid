package com.weeklylotto.app.domain.model

data class EvaluationResult(
    val rank: DrawRank,
    val matchedMainCount: Int,
    val bonusMatched: Boolean,
    val highlightedNumbers: Set<LottoNumber>,
)
