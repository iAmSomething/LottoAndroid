package com.weeklylotto.app.data.repository

import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.service.NumberGenerator
import kotlin.random.Random

class RandomNumberGenerator(
    private val random: Random = Random.Default,
) : NumberGenerator {
    override fun generateInitialGames(gameCount: Int): List<LottoGame> {
        val slots = GameSlot.entries.take(gameCount)
        return slots.map { slot ->
            LottoGame(
                slot = slot,
                numbers = randomNumbers(),
                lockedNumbers = emptySet(),
                mode = GameMode.AUTO,
            )
        }
    }

    override fun regenerateExceptLocked(games: List<LottoGame>): List<LottoGame> {
        return games.map { game ->
            if (game.lockedNumbers.isEmpty()) {
                game.copy(numbers = randomNumbers(), mode = GameMode.AUTO)
            } else {
                val unlockedCount = 6 - game.lockedNumbers.size
                val refillPool =
                    (1..45)
                        .asSequence()
                        .map(::LottoNumber)
                        .filter { it !in game.lockedNumbers }
                        .shuffled(random)
                        .take(unlockedCount)
                        .toList()

                val finalNumbers = (game.lockedNumbers + refillPool).sortedBy { it.value }
                game.copy(
                    numbers = finalNumbers,
                    mode = if (game.lockedNumbers.size == 6) GameMode.MANUAL else GameMode.SEMI_AUTO,
                )
            }
        }
    }

    private fun randomNumbers(): List<LottoNumber> {
        return (1..45)
            .shuffled(random)
            .take(6)
            .sorted()
            .map(::LottoNumber)
    }
}
