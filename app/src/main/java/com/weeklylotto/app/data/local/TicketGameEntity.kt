package com.weeklylotto.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ticket_game",
    foreignKeys = [
        ForeignKey(
            entity = TicketBundleEntity::class,
            parentColumns = ["id"],
            childColumns = ["bundleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["bundleId"])],
)
data class TicketGameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bundleId: Long,
    val slot: String,
    val numbers: List<Int>,
    val lockedNumbers: List<Int>,
    val mode: String,
)
