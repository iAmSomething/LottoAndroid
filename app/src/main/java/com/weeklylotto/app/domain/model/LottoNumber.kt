package com.weeklylotto.app.domain.model

@JvmInline
value class LottoNumber(val value: Int) {
    init {
        require(value in 1..45) { "로또 번호는 1~45 범위여야 합니다." }
    }
}
