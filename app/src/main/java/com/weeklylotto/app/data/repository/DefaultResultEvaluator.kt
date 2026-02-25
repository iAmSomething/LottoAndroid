package com.weeklylotto.app.data.repository

import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.EvaluationResult
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.service.ResultEvaluator

class DefaultResultEvaluator : ResultEvaluator {
    override fun evaluate(
        game: LottoGame,
        draw: DrawResult,
    ): EvaluationResult {
        val drawMainSet = draw.mainNumbers.toSet()
        val matched = game.numbers.filter { it in drawMainSet }.toSet()
        val bonusMatched = draw.bonus in game.numbers

        val rank =
            when {
                matched.size == 6 -> DrawRank.FIRST
                matched.size == 5 && bonusMatched -> DrawRank.SECOND
                matched.size == 5 -> DrawRank.THIRD
                matched.size == 4 -> DrawRank.FOURTH
                matched.size == 3 -> DrawRank.FIFTH
                else -> DrawRank.NONE
            }

        return EvaluationResult(
            rank = rank,
            matchedMainCount = matched.size,
            bonusMatched = bonusMatched,
            highlightedNumbers = matched,
        )
    }
}
