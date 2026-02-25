package com.weeklylotto.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrawDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DrawResultEntity)

    @Query("SELECT * FROM draw_result WHERE roundNumber = :roundNumber LIMIT 1")
    suspend fun findByRound(roundNumber: Int): DrawResultEntity?

    @Query("SELECT * FROM draw_result ORDER BY roundNumber DESC LIMIT 1")
    suspend fun latest(): DrawResultEntity?
}
