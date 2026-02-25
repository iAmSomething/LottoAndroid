package com.weeklylotto.app.domain.model

import java.time.LocalDate

data class Round(
    val number: Int,
    val drawDate: LocalDate,
)
