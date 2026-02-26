package com.weeklylotto.app.data.repository

import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.data.local.TicketDao
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsLogger
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.domain.service.NoOpAnalyticsLogger
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomTicketRepository(
    private val ticketDao: TicketDao,
    private val widgetRefreshScheduler: WidgetRefreshScheduler,
    private val analyticsLogger: AnalyticsLogger = NoOpAnalyticsLogger,
) : TicketRepository {
    override fun observeAllTickets(): Flow<List<TicketBundle>> =
        ticketDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> {
        val currentRound = RoundEstimator.currentSalesRound(LocalDate.now())
        return ticketDao.observeByRound(currentRound).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        ticketDao.observeByRound(round.number).map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(bundle: TicketBundle) {
        traceStorageMutation(operation = "save") {
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
                    return@traceStorageMutation
                }
            }

            val insertedId = ticketDao.insertBundle(bundle.toEntity())
            ticketDao.insertGames(bundle.games.map { it.toEntity(insertedId) })
            widgetRefreshScheduler.refreshAll()
        }
    }

    override suspend fun update(bundle: TicketBundle) {
        // TicketBundle은 immutable audit log로 보관한다.
        save(bundle)
    }

    override suspend fun latest(): TicketBundle? = ticketDao.latest()?.toDomain()

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) {
        if (ids.isEmpty()) {
            logStorageMutation(
                operation = "update_status",
                status = "skipped",
                startedAtNanos = System.nanoTime(),
            )
            return
        }
        traceStorageMutation(operation = "update_status") {
            ticketDao.updateBundleStatus(ids.toList(), status.name)
            widgetRefreshScheduler.refreshAll()
        }
    }

    override suspend fun deleteByIds(ids: Set<Long>) {
        if (ids.isEmpty()) {
            logStorageMutation(
                operation = "delete",
                status = "skipped",
                startedAtNanos = System.nanoTime(),
            )
            return
        }
        traceStorageMutation(operation = "delete") {
            ticketDao.deleteBundles(ids.toList())
            widgetRefreshScheduler.refreshAll()
        }
    }

    private suspend fun traceStorageMutation(
        operation: String,
        block: suspend () -> Unit,
    ) {
        val startedAtNanos = System.nanoTime()
        runCatching { block() }.fold(
            onSuccess = {
                logStorageMutation(
                    operation = operation,
                    status = "success",
                    startedAtNanos = startedAtNanos,
                )
            },
            onFailure = { throwable ->
                logStorageMutation(
                    operation = operation,
                    status = "failure",
                    startedAtNanos = startedAtNanos,
                    errorType = throwable::class.simpleName ?: "unknown",
                )
                throw throwable
            },
        )
    }

    private fun logStorageMutation(
        operation: String,
        status: String,
        startedAtNanos: Long,
        errorType: String? = null,
    ) {
        val latencyMs = ((System.nanoTime() - startedAtNanos) / 1_000_000L).coerceAtLeast(0L)
        val params =
            buildMap {
                put(AnalyticsParamKey.SCREEN, "system")
                put(AnalyticsParamKey.COMPONENT, "ticket_storage")
                put(AnalyticsParamKey.ACTION, "mutation")
                put(AnalyticsParamKey.OPERATION, operation)
                put(AnalyticsParamKey.STATUS, status)
                put(AnalyticsParamKey.LATENCY_MS, latencyMs.toString())
                if (errorType != null) {
                    put(AnalyticsParamKey.ERROR_TYPE, errorType)
                }
            }
        analyticsLogger.log(AnalyticsEvent.OPS_STORAGE_MUTATION, params)
    }
}
