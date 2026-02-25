package com.weeklylotto.app.data.repository

import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.data.local.DrawDao
import com.weeklylotto.app.data.local.DrawResultEntity
import com.weeklylotto.app.data.network.DrawApiClient
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.error.map
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler
import java.time.Instant
import java.time.LocalDate

class RemoteDrawRepository(
    private val drawDao: DrawDao,
    private val drawApiClient: DrawApiClient,
    private val widgetRefreshScheduler: WidgetRefreshScheduler,
) : DrawRepository {
    override suspend fun fetchLatest(): AppResult<DrawResult> {
        val estimated = RoundEstimator.estimate(LocalDate.now())
        val candidates = (estimated downTo (estimated - 2).coerceAtLeast(1))

        candidates.forEach { round ->
            when (val response = drawApiClient.fetchRound(round)) {
                is AppResult.Success -> {
                    val payload = response.value
                    val entity =
                        DrawResultEntity(
                            roundNumber = payload.round,
                            drawDate = payload.drawDate,
                            mainNumbers = payload.main,
                            bonusNumber = payload.bonus,
                            fetchedAtEpochMillis = Instant.now().toEpochMilli(),
                        )
                    drawDao.upsert(entity)
                    widgetRefreshScheduler.refreshAll()
                    return AppResult.Success(entity.toDomain())
                }
                is AppResult.Failure -> Unit
            }
        }

        return drawDao.latest()?.let { AppResult.Success(it.toDomain()) }
            ?: drawApiClient.fetchRound(estimated).map { payload ->
                val entity =
                    DrawResultEntity(
                        roundNumber = payload.round,
                        drawDate = payload.drawDate,
                        mainNumbers = payload.main,
                        bonusNumber = payload.bonus,
                        fetchedAtEpochMillis = Instant.now().toEpochMilli(),
                    )
                drawDao.upsert(entity)
                widgetRefreshScheduler.refreshAll()
                entity.toDomain()
            }
    }

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> {
        drawDao.findByRound(round.number)?.let { return AppResult.Success(it.toDomain()) }

        return drawApiClient.fetchRound(round.number).map { payload ->
            val entity =
                DrawResultEntity(
                    roundNumber = payload.round,
                    drawDate = payload.drawDate,
                    mainNumbers = payload.main,
                    bonusNumber = payload.bonus,
                    fetchedAtEpochMillis = Instant.now().toEpochMilli(),
                )
            drawDao.upsert(entity)
            widgetRefreshScheduler.refreshAll()
            entity.toDomain()
        }
    }
}

private fun DrawResultEntity.toDomain(): DrawResult =
    DrawResult(
        round =
            Round(
                number = roundNumber,
                drawDate = LocalDate.parse(drawDate),
            ),
        mainNumbers = mainNumbers.map(::LottoNumber),
        bonus = LottoNumber(bonusNumber),
        drawDate = LocalDate.parse(drawDate),
    )
