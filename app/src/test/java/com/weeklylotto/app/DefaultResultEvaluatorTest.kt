package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.repository.DefaultResultEvaluator
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import org.junit.Test
import java.time.LocalDate

class DefaultResultEvaluatorTest {
    private val evaluator = DefaultResultEvaluator()
    private val draw =
        DrawResult(
            round = Round(1099, LocalDate.parse("2026-02-21")),
            mainNumbers = listOf(3, 20, 28, 38, 40, 43).map(::LottoNumber),
            bonus = LottoNumber(26),
            drawDate = LocalDate.parse("2026-02-21"),
        )

    @Test
    fun `5개 메인 번호가 맞으면 3등이다`() {
        val game =
            LottoGame(
                slot = GameSlot.A,
                numbers = listOf(3, 20, 28, 38, 40, 1).map(::LottoNumber),
            )

        val result = evaluator.evaluate(game, draw)

        assertThat(result.rank).isEqualTo(DrawRank.THIRD)
        assertThat(result.matchedMainCount).isEqualTo(5)
        assertThat(result.bonusMatched).isFalse()
    }

    @Test
    fun `5개 메인 + 보너스가 맞으면 2등이다`() {
        val game =
            LottoGame(
                slot = GameSlot.A,
                numbers = listOf(3, 20, 28, 38, 40, 26).map(::LottoNumber),
            )

        val result = evaluator.evaluate(game, draw)

        assertThat(result.rank).isEqualTo(DrawRank.SECOND)
        assertThat(result.matchedMainCount).isEqualTo(5)
        assertThat(result.bonusMatched).isTrue()
    }

    @Test
    fun `3개 메인 번호가 맞으면 5등이다`() {
        val game =
            LottoGame(
                slot = GameSlot.A,
                numbers = listOf(3, 20, 28, 1, 2, 4).map(::LottoNumber),
            )

        val result = evaluator.evaluate(game, draw)

        assertThat(result.rank).isEqualTo(DrawRank.FIFTH)
    }
}
