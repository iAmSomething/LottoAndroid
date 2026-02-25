package com.weeklylotto.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket_bundle")
data class TicketBundleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roundNumber: Int,
    val drawDate: String,
    val source: String,
    val status: String,
    val createdAtEpochMillis: Long,
)
