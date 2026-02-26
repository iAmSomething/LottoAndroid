package com.weeklylotto.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert
    suspend fun insertBundle(bundle: TicketBundleEntity): Long

    @Insert
    suspend fun insertGames(games: List<TicketGameEntity>)

    @Query(
        "SELECT * FROM ticket_bundle " +
            "WHERE roundNumber = :roundNumber AND source = :source " +
            "ORDER BY createdAtEpochMillis DESC LIMIT 1",
    )
    suspend fun latestByRoundAndSource(
        roundNumber: Int,
        source: String,
    ): TicketBundleEntity?

    @Update
    suspend fun updateBundle(bundle: TicketBundleEntity)

    @Query("DELETE FROM ticket_game WHERE bundleId = :bundleId")
    suspend fun deleteGamesByBundle(bundleId: Long)

    @Query("DELETE FROM ticket_bundle WHERE id IN (:ids)")
    suspend fun deleteBundles(ids: List<Long>)

    @Query("UPDATE ticket_bundle SET status = :status WHERE id IN (:ids)")
    suspend fun updateBundleStatus(
        ids: List<Long>,
        status: String,
    )

    @Transaction
    @Query("SELECT * FROM ticket_bundle ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<TicketBundleWithGames>>

    @Transaction
    @Query("SELECT * FROM ticket_bundle WHERE roundNumber = :roundNumber ORDER BY createdAtEpochMillis DESC")
    fun observeByRound(roundNumber: Int): Flow<List<TicketBundleWithGames>>

    @Transaction
    @Query("SELECT * FROM ticket_bundle ORDER BY createdAtEpochMillis DESC LIMIT 1")
    suspend fun latest(): TicketBundleWithGames?
}
