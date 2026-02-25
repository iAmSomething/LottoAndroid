package com.weeklylotto.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draw_result")
data class DrawResultEntity(
    @PrimaryKey
    val roundNumber: Int,
    val drawDate: String,
    val mainNumbers: List<Int>,
    val bonusNumber: Int,
    val fetchedAtEpochMillis: Long,
)
