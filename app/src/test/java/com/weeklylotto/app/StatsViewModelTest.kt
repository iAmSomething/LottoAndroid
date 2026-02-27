package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
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
import com.weeklylotto.app.feature.stats.StatsPeriod
import com.weeklylotto.app.feature.stats.StatsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 전체기간_통계가_구매금_당첨금_게임수를_계산한다() =
        runTest {
            val round = Round(1202, LocalDate.of(2026, 3, 21))
            val bundle1 = ticket(round, listOf(3, 14, 25, 31, 38, 42), Instant.now())
            val bundle2 = ticket(round, listOf(1, 2, 3, 4, 5, 6), Instant.now())

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(bundle1, bundle2)),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            val state = viewModel.uiState.value

            assertThat(state.totalGames).isEqualTo(2)
            assertThat(state.totalPurchaseAmount).isEqualTo(2_000L)
            assertThat(state.totalWinAmount).isEqualTo(5_000L)
            assertThat(state.winningGames).isEqualTo(1)
        }

    @Test
    fun 최근4주_필터는_오래된_번들을_제외한다() =
        runTest {
            val round = Round(1203, LocalDate.of(2026, 3, 28))
            val recent = ticket(round, listOf(3, 14, 25, 31, 38, 42), Instant.now())
            val old = ticket(round, listOf(7, 8, 9, 10, 11, 12), Instant.now().minus(40, ChronoUnit.DAYS))

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(recent, old)),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            viewModel.setPeriod(StatsPeriod.RECENT_4_WEEKS)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.selectedPeriod).isEqualTo(StatsPeriod.RECENT_4_WEEKS)
            assertThat(state.totalGames).isEqualTo(1)
            assertThat(state.totalPurchaseAmount).isEqualTo(1_000L)
        }

    @Test
    fun 최근8주_필터는_4주를_초과한_데이터를_포함한다() =
        runTest {
            val round = Round(1204, LocalDate.of(2026, 4, 4))
            val within8Weeks = ticket(round, listOf(3, 14, 25, 31, 38, 42), Instant.now().minus(40, ChronoUnit.DAYS))
            val olderThan8Weeks = ticket(round, listOf(7, 8, 9, 10, 11, 12), Instant.now().minus(70, ChronoUnit.DAYS))

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(within8Weeks, olderThan8Weeks)),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            viewModel.setPeriod(StatsPeriod.RECENT_8_WEEKS)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.selectedPeriod).isEqualTo(StatsPeriod.RECENT_8_WEEKS)
            assertThat(state.totalGames).isEqualTo(1)
            assertThat(state.totalPurchaseAmount).isEqualTo(1_000L)
        }

    @Test
    fun 커스텀_회차_필터는_지정_범위만_포함한다() =
        runTest {
            val bundle1203 =
                ticket(
                    round = Round(1203, LocalDate.of(2026, 3, 28)),
                    numbers = listOf(3, 14, 25, 31, 38, 42),
                    createdAt = Instant.now(),
                )
            val bundle1205 =
                ticket(
                    round = Round(1205, LocalDate.of(2026, 4, 11)),
                    numbers = listOf(7, 8, 9, 10, 11, 12),
                    createdAt = Instant.now(),
                )

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(bundle1203, bundle1205)),
                    drawRepository = StatsDrawRepository(draw(bundle1205.round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            viewModel.updateCustomRoundRange(startRound = "1203", endRound = "1204")
            viewModel.applyCustomRoundRange()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.selectedPeriod).isEqualTo(StatsPeriod.CUSTOM)
            assertThat(state.customRangeError).isNull()
            assertThat(state.totalGames).isEqualTo(1)
            assertThat(state.totalPurchaseAmount).isEqualTo(1_000L)
        }

    @Test
    fun 커스텀_회차_필터는_잘못된_범위면_오류를_표시한다() =
        runTest {
            val round = Round(1206, LocalDate.of(2026, 4, 18))
            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(ticket(round, listOf(1, 2, 3, 4, 5, 6), Instant.now()))),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            viewModel.updateCustomRoundRange(startRound = "1210", endRound = "1200")
            viewModel.applyCustomRoundRange()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.selectedPeriod).isEqualTo(StatsPeriod.ALL)
            assertThat(state.customRangeError).isEqualTo("시작 회차는 끝 회차보다 클 수 없습니다.")
        }

    @Test
    fun 출처별_성과가_자동_수동_QR로_집계된다() =
        runTest {
            val round = Round(1205, LocalDate.of(2026, 4, 11))
            val generatedWin = ticket(round, listOf(3, 14, 25, 31, 38, 42), Instant.now(), TicketSource.GENERATED)
            val manualLose = ticket(round, listOf(1, 2, 3, 4, 5, 6), Instant.now(), TicketSource.MANUAL)
            val qrLose = ticket(round, listOf(7, 8, 9, 10, 11, 12), Instant.now(), TicketSource.QR_SCAN)

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(generatedWin, manualLose, qrLose)),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            val state = viewModel.uiState.value

            val generated = state.sourceStats.first { it.source == TicketSource.GENERATED }
            val manual = state.sourceStats.first { it.source == TicketSource.MANUAL }
            val qr = state.sourceStats.first { it.source == TicketSource.QR_SCAN }

            assertThat(generated.totalGames).isEqualTo(1)
            assertThat(generated.winningGames).isEqualTo(1)
            assertThat(generated.totalPurchaseAmount).isEqualTo(1_000L)
            assertThat(generated.totalWinAmount).isEqualTo(5_000L)
            assertThat(generated.netProfitAmount).isEqualTo(4_000L)
            assertThat(generated.winRatePercent).isEqualTo(100)
            assertThat(generated.roiPercent).isEqualTo(400)

            assertThat(manual.totalGames).isEqualTo(1)
            assertThat(manual.winningGames).isEqualTo(0)
            assertThat(manual.totalPurchaseAmount).isEqualTo(1_000L)
            assertThat(manual.totalWinAmount).isEqualTo(0L)
            assertThat(manual.netProfitAmount).isEqualTo(-1_000L)
            assertThat(manual.winRatePercent).isEqualTo(0)
            assertThat(manual.roiPercent).isEqualTo(-100)

            assertThat(qr.totalGames).isEqualTo(1)
            assertThat(qr.winningGames).isEqualTo(0)
            assertThat(qr.totalPurchaseAmount).isEqualTo(1_000L)
            assertThat(qr.totalWinAmount).isEqualTo(0L)
            assertThat(qr.netProfitAmount).isEqualTo(-1_000L)
            assertThat(qr.winRatePercent).isEqualTo(0)
            assertThat(qr.roiPercent).isEqualTo(-100)
        }

    @Test
    fun 출처별_데이터가_없으면_기본_3개_출처를_0값으로_유지한다() =
        runTest {
            val round = Round(1206, LocalDate.of(2026, 4, 18))

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(emptyList()),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            val state = viewModel.uiState.value

            assertThat(state.sourceStats).hasSize(3)
            assertThat(state.sourceStats.map { it.source })
                .containsExactly(TicketSource.GENERATED, TicketSource.QR_SCAN, TicketSource.MANUAL)
                .inOrder()
            assertThat(
                state.sourceStats.all {
                    it.totalGames == 0 && it.totalPurchaseAmount == 0L && it.totalWinAmount == 0L
                },
            )
                .isTrue()
        }

    @Test
    fun ROI_트렌드는_회차순으로_최대_8개를_유지한다() =
        runTest {
            val bundles =
                (1200..1209).map { roundNumber ->
                    ticket(
                        round = Round(roundNumber, LocalDate.of(2026, 1, 1).plusWeeks((roundNumber - 1200).toLong())),
                        numbers = listOf(1, 2, 3, 4, 5, ((roundNumber - 1200) % 40) + 6),
                        createdAt = Instant.now(),
                        source = TicketSource.GENERATED,
                    )
                }
            val latestRound = bundles.maxBy { it.round.number }.round

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(bundles),
                    drawRepository = StatsDrawRepository(draw(latestRound)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            val trend = viewModel.uiState.value.roiTrend

            assertThat(trend).hasSize(8)
            assertThat(trend.first().round).isEqualTo(1202)
            assertThat(trend.last().round).isEqualTo(1209)
            assertThat(trend.all { it.totalPurchaseAmount == 1_000L }).isTrue()
            assertThat(trend.all { it.roiPercent == -100 }).isTrue()
        }

    @Test
    fun ROI_트렌드는_데이터가_없으면_빈목록이다() =
        runTest {
            val round = Round(1210, LocalDate.of(2026, 4, 25))

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(emptyList()),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            assertThat(viewModel.uiState.value.roiTrend).isEmpty()
        }

    @Test
    fun 번호_구간_분포는_구간별로_개수와_비율을_집계한다() =
        runTest {
            val round = Round(1211, LocalDate.of(2026, 5, 2))
            val bundle = ticket(round, listOf(1, 9, 10, 19, 20, 45), Instant.now())

            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(listOf(bundle)),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            val distribution = viewModel.uiState.value.numberDistribution

            assertThat(distribution).hasSize(5)
            assertThat(distribution[0].label).isEqualTo("1-9")
            assertThat(distribution[0].count).isEqualTo(2)
            assertThat(distribution[0].percent).isEqualTo(33)
            assertThat(distribution[1].label).isEqualTo("10-19")
            assertThat(distribution[1].count).isEqualTo(2)
            assertThat(distribution[1].percent).isEqualTo(33)
            assertThat(distribution[2].label).isEqualTo("20-29")
            assertThat(distribution[2].count).isEqualTo(1)
            assertThat(distribution[2].percent).isEqualTo(16)
            assertThat(distribution[3].label).isEqualTo("30-39")
            assertThat(distribution[3].count).isEqualTo(0)
            assertThat(distribution[4].label).isEqualTo("40-45")
            assertThat(distribution[4].count).isEqualTo(1)
            assertThat(distribution[4].percent).isEqualTo(16)
        }

    @Test
    fun 번호_구간_분포는_데이터가_없어도_기본_구간을_유지한다() =
        runTest {
            val round = Round(1212, LocalDate.of(2026, 5, 9))
            val viewModel =
                StatsViewModel(
                    ticketRepository = StatsTicketRepository(emptyList()),
                    drawRepository = StatsDrawRepository(draw(round)),
                    resultEvaluator = OneWinEvaluator,
                )

            advanceUntilIdle()
            val distribution = viewModel.uiState.value.numberDistribution

            assertThat(distribution).hasSize(5)
            assertThat(distribution.all { it.count == 0 && it.percent == 0 }).isTrue()
        }
}

private fun draw(round: Round): DrawResult =
    DrawResult(
        round = round,
        mainNumbers = listOf(3, 14, 25, 31, 38, 42).map(::LottoNumber),
        bonus = LottoNumber(7),
        drawDate = round.drawDate,
    )

private fun ticket(
    round: Round,
    numbers: List<Int>,
    createdAt: Instant,
    source: TicketSource = TicketSource.GENERATED,
): TicketBundle =
    TicketBundle(
        round = round,
        games = listOf(LottoGame(slot = GameSlot.A, numbers = numbers.map(::LottoNumber))),
        source = source,
        createdAt = createdAt,
    )

private class StatsTicketRepository(
    bundles: List<TicketBundle>,
) : TicketRepository {
    private val all = MutableStateFlow(bundles)

    override fun observeAllTickets(): Flow<List<TicketBundle>> = all

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = all

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        flowOf(all.value.filter { it.round.number == round.number })

    override suspend fun save(bundle: TicketBundle) = Unit

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = all.value.firstOrNull()

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}

private class StatsDrawRepository(
    private val draw: DrawResult,
) : DrawRepository {
    override suspend fun fetchLatest(): AppResult<DrawResult> = AppResult.Success(draw)

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> = AppResult.Success(draw)
}

private object OneWinEvaluator : ResultEvaluator {
    override fun evaluate(
        game: LottoGame,
        drawResult: DrawResult,
    ): EvaluationResult {
        val win = game.numbers.map { it.value } == listOf(3, 14, 25, 31, 38, 42)
        return EvaluationResult(
            rank = if (win) DrawRank.FIFTH else DrawRank.NONE,
            matchedMainCount = if (win) 3 else 0,
            bonusMatched = false,
            highlightedNumbers = emptySet(),
        )
    }
}
