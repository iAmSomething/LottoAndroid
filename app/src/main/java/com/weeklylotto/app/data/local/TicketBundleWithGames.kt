package com.weeklylotto.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TicketBundleWithGames(
    @Embedded
    val bundle: TicketBundleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "bundleId",
    )
    val games: List<TicketGameEntity>,
)
