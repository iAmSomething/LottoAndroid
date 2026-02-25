package com.weeklylotto.app.data.repository

import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.data.local.TicketDao
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomTicketRepository(
    private val ticketDao: TicketDao,
    private val widgetRefreshScheduler: WidgetRefreshScheduler,
) : TicketRepository {
    override fun observeAllTickets(): Flow<List<TicketBundle>> =
        ticketDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> {
        val currentRound = RoundEstimator.estimate(LocalDate.now())
        return ticketDao.observeByRound(currentRound).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        ticketDao.observeByRound(round.number).map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(bundle: TicketBundle) {
        if (bundle.source == TicketSource.GENERATED) {
            val existingGenerated =
                ticketDao.latestByRoundAndSource(
                    roundNumber = bundle.round.number,
                    source = TicketSource.GENERATED.name,
                )

            if (existingGenerated != null) {
                ticketDao.updateBundle(bundle.toEntity().copy(id = existingGenerated.id))
                ticketDao.deleteGamesByBundle(existingGenerated.id)
                ticketDao.insertGames(bundle.games.map { it.toEntity(existingGenerated.id) })
                widgetRefreshScheduler.refreshAll()
                return
            }
        }

        val insertedId = ticketDao.insertBundle(bundle.toEntity())
        ticketDao.insertGames(bundle.games.map { it.toEntity(insertedId) })
        widgetRefreshScheduler.refreshAll()
    }

    override suspend fun update(bundle: TicketBundle) {
        // TicketBundle은 immutable audit log로 보관한다.
        save(bundle)
    }

    override suspend fun latest(): TicketBundle? = ticketDao.latest()?.toDomain()

    override suspend fun deleteByIds(ids: Set<Long>) {
        if (ids.isEmpty()) return
        ticketDao.deleteBundles(ids.toList())
        widgetRefreshScheduler.refreshAll()
    }
}
