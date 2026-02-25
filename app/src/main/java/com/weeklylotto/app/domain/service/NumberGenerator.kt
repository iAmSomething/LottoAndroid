package com.weeklylotto.app.domain.service

import com.weeklylotto.app.domain.model.LottoGame

interface NumberGenerator {
    fun generateInitialGames(gameCount: Int = 5): List<LottoGame>

    fun regenerateExceptLocked(games: List<LottoGame>): List<LottoGame>
}
