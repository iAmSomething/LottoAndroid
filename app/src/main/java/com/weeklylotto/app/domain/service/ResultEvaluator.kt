package com.weeklylotto.app.domain.service

import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.EvaluationResult
import com.weeklylotto.app.domain.model.LottoGame

interface ResultEvaluator {
    fun evaluate(
        game: LottoGame,
        draw: DrawResult,
    ): EvaluationResult
}
