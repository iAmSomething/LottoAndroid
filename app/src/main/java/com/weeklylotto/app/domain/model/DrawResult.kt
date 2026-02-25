package com.weeklylotto.app.domain.model

import java.time.LocalDate

data class DrawResult(
    val round: Round,
    val mainNumbers: List<LottoNumber>,
    val bonus: LottoNumber,
    val drawDate: LocalDate,
) {
    init {
        require(mainNumbers.size == 6) { "당첨 번호는 6개여야 합니다." }
        require(mainNumbers.distinct().size == 6) { "당첨 번호는 중복될 수 없습니다." }
        require(mainNumbers.none { it == bonus }) { "보너스 번호는 메인 번호와 중복될 수 없습니다." }
    }
}
