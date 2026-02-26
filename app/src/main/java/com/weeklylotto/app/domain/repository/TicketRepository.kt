package com.weeklylotto.app.domain.repository

import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketStatus
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun observeAllTickets(): Flow<List<TicketBundle>>

    fun observeCurrentRoundTickets(): Flow<List<TicketBundle>>

    fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>>

    suspend fun save(bundle: TicketBundle)

    suspend fun update(bundle: TicketBundle)

    suspend fun latest(): TicketBundle?

    suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    )

    suspend fun deleteByIds(ids: Set<Long>)
}
