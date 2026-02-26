package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.repository.DefaultWidgetDataProvider
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.EvaluationResult
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.ResultEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class DefaultWidgetDataProviderTest {
    @Test
    fun 주간위젯_스냅샷은_현재회차_라벨과_번들목록을_반환한다() =
        runTest {
            val round = Round(1200, LocalDate.of(2026, 3, 7))
            val bundle = bundle(round, listOf(1, 2, 3, 4, 5, 6))
            val provider =
                DefaultWidgetDataProvider(
                    ticketRepository =
                        WidgetFakeTicketRepository(
                            currentRound = listOf(bundle),
                            byRound = listOf(bundle),
                        ),
                    drawRepository = FakeDrawRepository(successDraw(round)),
                    resultEvaluator = AlwaysFifthEvaluator,
                )

            val snapshot = provider.loadWeeklyNumbersSnapshot()

            assertThat(snapshot.roundLabel).isEqualTo("이번주 1200회")
            assertThat(snapshot.bundles).hasSize(1)
        }

    @Test
    fun 결과요약_스냅샷은_당첨시_등수요약을_반환한다() =
        runTest {
            val round = Round(1201, LocalDate.of(2026, 3, 14))
            val bundle = bundle(round, listOf(3, 14, 25, 31, 38, 42))
            val provider =
                DefaultWidgetDataProvider(
                    ticketRepository = WidgetFakeTicketRepository(currentRound = emptyList(), byRound = listOf(bundle)),
                    drawRepository = FakeDrawRepository(successDraw(round)),
                    resultEvaluator = AlwaysFifthEvaluator,
                )

            val snapshot = provider.loadResultSummarySnapshot()

            assertThat(snapshot.roundLabel).isEqualTo("1201회 결과")
            assertThat(snapshot.summaryText).isEqualTo("5등 당첨!")
        }
}

private fun successDraw(round: Round): DrawResult =
    DrawResult(
        round = round,
        mainNumbers = listOf(3, 14, 25, 31, 38, 42).map(::LottoNumber),
        bonus = LottoNumber(7),
        drawDate = round.drawDate,
    )

private fun bundle(
    round: Round,
    numbers: List<Int>,
): TicketBundle =
    TicketBundle(
        round = round,
        games =
            listOf(
                LottoGame(
                    slot = GameSlot.A,
                    numbers = numbers.map(::LottoNumber),
                ),
            ),
        source = TicketSource.GENERATED,
    )

private class FakeDrawRepository(
    private val draw: DrawResult,
) : DrawRepository {
    override suspend fun fetchLatest(): AppResult<DrawResult> = AppResult.Success(draw)

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> = AppResult.Success(draw)
}

private class WidgetFakeTicketRepository(
    private val currentRound: List<TicketBundle>,
    private val byRound: List<TicketBundle>,
) : TicketRepository {
    override fun observeAllTickets(): Flow<List<TicketBundle>> = flowOf(currentRound + byRound)

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = flowOf(currentRound)

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> = flowOf(byRound)

    override suspend fun save(bundle: TicketBundle) = Unit

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = byRound.firstOrNull()

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}

private object AlwaysFifthEvaluator : ResultEvaluator {
    override fun evaluate(
        game: LottoGame,
        drawResult: DrawResult,
    ): EvaluationResult =
        EvaluationResult(
            rank = DrawRank.FIFTH,
            matchedMainCount = 3,
            bonusMatched = false,
            highlightedNumbers = emptySet(),
        )
}
