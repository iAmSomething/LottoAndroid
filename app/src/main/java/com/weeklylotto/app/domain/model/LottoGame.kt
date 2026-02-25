package com.weeklylotto.app.domain.model

data class LottoGame(
    val slot: GameSlot,
    val numbers: List<LottoNumber>,
    val lockedNumbers: Set<LottoNumber> = emptySet(),
    val mode: GameMode = GameMode.AUTO,
) {
    init {
        require(numbers.size == 6) { "게임은 6개 번호를 가져야 합니다." }
        require(numbers.distinct().size == 6) { "게임 번호는 중복될 수 없습니다." }
    }
}
