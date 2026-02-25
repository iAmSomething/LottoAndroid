package com.weeklylotto.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TicketBundleEntity::class,
        TicketGameEntity::class,
        DrawResultEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class WeeklyLottoDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao

    abstract fun drawDao(): DrawDao
}
