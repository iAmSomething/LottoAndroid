package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.repository.RandomNumberGenerator
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import org.junit.Test
import kotlin.random.Random

class RandomNumberGeneratorTest {
    private val generator = RandomNumberGenerator(Random(1))

    @Test
    fun `초기 게임은 5개 슬롯과 6개 고유번호를 생성한다`() {
        val games = generator.generateInitialGames(5)

        assertThat(games).hasSize(5)
        assertThat(games.map { it.slot }).containsExactly(
            GameSlot.A,
            GameSlot.B,
            GameSlot.C,
            GameSlot.D,
            GameSlot.E,
        )

        games.forEach { game ->
            assertThat(game.mode).isEqualTo(GameMode.AUTO)
            assertThat(game.numbers).hasSize(6)
            assertThat(game.numbers.map { it.value }.distinct()).hasSize(6)
            assertThat(game.numbers.map { it.value }.all { it in 1..45 }).isTrue()
        }
    }

    @Test
    fun `잠금 번호는 재생성 후에도 유지된다`() {
        val game = generator.generateInitialGames(1).first()
        val locked = setOf(game.numbers.first(), game.numbers[1])
        val lockedGame = game.copy(lockedNumbers = locked, mode = GameMode.SEMI_AUTO)

        val regenerated = generator.regenerateExceptLocked(listOf(lockedGame)).first()

        assertThat(regenerated.lockedNumbers).containsExactlyElementsIn(locked)
        assertThat(regenerated.numbers).containsAtLeastElementsIn(locked)
        assertThat(regenerated.numbers).hasSize(6)
    }
}
