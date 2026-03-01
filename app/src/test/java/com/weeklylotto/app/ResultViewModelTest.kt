package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.error.AppError
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
import com.weeklylotto.app.domain.service.ResultViewTracker
import com.weeklylotto.app.feature.result.ResultViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 네트워크_실패후_재시도_성공시_결과를_노출한다() =
        runTest {
            val draw = sampleDraw(roundNumber = 1212)
            val drawRepository =
                ResultViewModelFakeDrawRepository(
                    latestQueue =
                        ArrayDeque(
                            listOf(
                                AppResult.Failure(AppError.NetworkError("timeout", 504)),
                                AppResult.Success(draw),
                            ),
                        ),
                )
            val viewModel =
                ResultViewModel(
                    drawRepository = drawRepository,
                    ticketRepository = ResultViewModelFakeTicketRepository(),
                    evaluator = ResultViewModelFakeResultEvaluator(),
                    retryDelayProvider = { 0L },
                    nowProvider = { LocalDateTime.of(2026, 2, 26, 9, 0, 0) },
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(drawRepository.fetchLatestCount).isEqualTo(2)
            assertThat(state.drawResult?.round?.number).isEqualTo(1212)
            assertThat(state.error).isNull()
            assertThat(state.lastErrorAt).isNull()
            assertThat(state.retryAttempt).isEqualTo(0)
        }

    @Test
    fun 네트워크_연속_실패시_최대_재시도후_에러를_노출한다() =
        runTest {
            val drawRepository =
                ResultViewModelFakeDrawRepository(
                    latestQueue =
                        ArrayDeque(
                            listOf(
                                AppResult.Failure(AppError.NetworkError("unavailable", 503)),
                                AppResult.Failure(AppError.NetworkError("unavailable", 503)),
                                AppResult.Failure(AppError.NetworkError("unavailable", 503)),
                            ),
                        ),
                )
            val failedAt = LocalDateTime.of(2026, 2, 26, 9, 30, 0)
            val viewModel =
                ResultViewModel(
                    drawRepository = drawRepository,
                    ticketRepository = ResultViewModelFakeTicketRepository(),
                    evaluator = ResultViewModelFakeResultEvaluator(),
                    retryDelayProvider = { 0L },
                    nowProvider = { failedAt },
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(drawRepository.fetchLatestCount).isEqualTo(3)
            assertThat(state.drawResult).isNull()
            assertThat(state.error).isNotNull()
            assertThat(state.retryAttempt).isEqualTo(3)
            assertThat(state.lastErrorAt).isEqualTo(failedAt)
        }

    @Test
    fun 회차선택시_fetchByRound로_조회하고_선택회차를_갱신한다() =
        runTest {
            val latest = sampleDraw(roundNumber = 1212)
            val selected = sampleDraw(roundNumber = 1209)
            val drawRepository =
                ResultViewModelFakeDrawRepository(
                    latestQueue = ArrayDeque(listOf(AppResult.Success(latest))),
                    byRoundResponses = mapOf(1209 to AppResult.Success(selected)),
                )
            val viewModel =
                ResultViewModel(
                    drawRepository = drawRepository,
                    ticketRepository = ResultViewModelFakeTicketRepository(),
                    evaluator = ResultViewModelFakeResultEvaluator(),
                    retryDelayProvider = { 0L },
                )

            advanceUntilIdle()
            viewModel.selectRound(1209)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(drawRepository.fetchByRoundCount).isEqualTo(1)
            assertThat(drawRepository.lastRequestedRound).isEqualTo(1209)
            assertThat(state.selectedRound).isEqualTo(1209)
            assertThat(state.drawResult?.round?.number).isEqualTo(1209)
            assertThat(state.availableRounds).contains(1212)
            assertThat(state.availableRounds).contains(1209)
        }

    @Test
    fun 회차선택후_refresh는_선택회차를_유지해_재조회한다() =
        runTest {
            val latest = sampleDraw(roundNumber = 1212)
            val selected = sampleDraw(roundNumber = 1210)
            val drawRepository =
                ResultViewModelFakeDrawRepository(
                    latestQueue = ArrayDeque(listOf(AppResult.Success(latest))),
                    byRoundResponses = mapOf(1210 to AppResult.Success(selected)),
                )
            val viewModel =
                ResultViewModel(
                    drawRepository = drawRepository,
                    ticketRepository = ResultViewModelFakeTicketRepository(),
                    evaluator = ResultViewModelFakeResultEvaluator(),
                    retryDelayProvider = { 0L },
                )

            advanceUntilIdle()
            viewModel.selectRound(1210)
            advanceUntilIdle()
            viewModel.refresh()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(drawRepository.fetchLatestCount).isEqualTo(1)
            assertThat(drawRepository.fetchByRoundCount).isEqualTo(2)
            assertThat(state.selectedRound).isEqualTo(1210)
            assertThat(state.drawResult?.round?.number).isEqualTo(1210)
        }

    @Test
    fun 당첨게임이_있으면_예상당첨금합계를_계산한다() =
        runTest {
            val draw = sampleDraw(roundNumber = 1212)
            val drawRepository =
                ResultViewModelFakeDrawRepository(
                    latestQueue = ArrayDeque(listOf(AppResult.Success(draw))),
                )
            val ticketRepository =
                ResultViewModelFakeTicketRepository(
                    tickets =
                        listOf(
                            TicketBundle(
                                id = 1L,
                                round = draw.round,
                                source = TicketSource.GENERATED,
                                games =
                                    listOf(
                                        LottoGame(
                                            slot = GameSlot.A,
                                            numbers = listOf(3, 12, 19, 24, 35, 41).map(::LottoNumber),
                                        ),
                                    ),
                            ),
                        ),
                )
            val viewModel =
                ResultViewModel(
                    drawRepository = drawRepository,
                    ticketRepository = ticketRepository,
                    evaluator = ResultViewModelFakeResultEvaluator(rank = DrawRank.FIFTH),
                    retryDelayProvider = { 0L },
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.winningCount).isEqualTo(1)
            assertThat(state.totalWinningAmount).isEqualTo(5_000L)
        }

    @Test
    fun 초기진입시_최근조회회차가_있으면_해당회차를_우선_조회한다() =
        runTest {
            val latest = sampleDraw(roundNumber = 1212)
            val lastViewed = sampleDraw(roundNumber = 1208)
            val drawRepository =
                ResultViewModelFakeDrawRepository(
                    latestQueue = ArrayDeque(listOf(AppResult.Success(latest))),
                    byRoundResponses = mapOf(1208 to AppResult.Success(lastViewed)),
                )
            val tracker = ResultViewModelFakeResultViewTracker(lastViewedRound = 1208)
            val viewModel =
                ResultViewModel(
                    drawRepository = drawRepository,
                    ticketRepository = ResultViewModelFakeTicketRepository(),
                    evaluator = ResultViewModelFakeResultEvaluator(),
                    resultViewTracker = tracker,
                    retryDelayProvider = { 0L },
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(drawRepository.fetchByRoundCount).isEqualTo(1)
            assertThat(drawRepository.lastRequestedRound).isEqualTo(1208)
            assertThat(drawRepository.fetchLatestCount).isEqualTo(0)
            assertThat(state.selectedRound).isEqualTo(1208)
            assertThat(state.drawResult?.round?.number).isEqualTo(1208)
        }

    @Test
    fun 결과조회_성공시_확인회차를_기록한다() =
        runTest {
            val draw = sampleDraw(roundNumber = 1212)
            val tracker = ResultViewModelFakeResultViewTracker()
            val viewModel =
                ResultViewModel(
                    drawRepository =
                        ResultViewModelFakeDrawRepository(
                            latestQueue = ArrayDeque(listOf(AppResult.Success(draw))),
                        ),
                    ticketRepository = ResultViewModelFakeTicketRepository(),
                    evaluator = ResultViewModelFakeResultEvaluator(),
                    resultViewTracker = tracker,
                    retryDelayProvider = { 0L },
                )

            advanceUntilIdle()

            assertThat(tracker.markedRounds).containsExactly(1212)
        }
}

private class ResultViewModelFakeDrawRepository(
    private val latestQueue: ArrayDeque<AppResult<DrawResult>>,
    private val byRoundResponses: Map<Int, AppResult<DrawResult>> = emptyMap(),
) : DrawRepository {
    var fetchLatestCount: Int = 0
    var fetchByRoundCount: Int = 0
    var lastRequestedRound: Int? = null

    override suspend fun fetchLatest(): AppResult<DrawResult> {
        fetchLatestCount += 1
        return latestQueue.removeFirstOrNull()
            ?: AppResult.Failure(AppError.NetworkError("no more queued response"))
    }

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> {
        fetchByRoundCount += 1
        lastRequestedRound = round.number
        return byRoundResponses[round.number] ?: fetchLatest()
    }
}

private class ResultViewModelFakeTicketRepository(
    private val tickets: List<TicketBundle> = emptyList(),
) : TicketRepository {
    override fun observeAllTickets(): Flow<List<TicketBundle>> = flowOf(tickets)

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = flowOf(tickets)

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        flowOf(tickets.filter { it.round.number == round.number })

    override suspend fun save(bundle: TicketBundle) = Unit

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = null

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}

private class ResultViewModelFakeResultEvaluator(
    private val rank: DrawRank = DrawRank.NONE,
) : ResultEvaluator {
    override fun evaluate(
        game: LottoGame,
        draw: DrawResult,
    ): EvaluationResult =
        EvaluationResult(
            rank = rank,
            matchedMainCount = 0,
            bonusMatched = false,
            highlightedNumbers = emptySet(),
        )
}

private class ResultViewModelFakeResultViewTracker(
    private var lastViewedRound: Int? = null,
) : ResultViewTracker {
    val markedRounds = mutableListOf<Int>()

    override suspend fun loadLastViewedRound(): Int? = lastViewedRound

    override suspend fun loadRecentViewedRounds(limit: Int): List<Int> = listOfNotNull(lastViewedRound).take(limit)

    override suspend fun markRoundViewed(roundNumber: Int) {
        markedRounds += roundNumber
        lastViewedRound = roundNumber
    }
}

private fun sampleDraw(roundNumber: Int): DrawResult =
    DrawResult(
        round = Round(roundNumber, LocalDate.of(2026, 2, 21)),
        mainNumbers = listOf(3, 12, 19, 24, 35, 41).map(::LottoNumber),
        bonus = LottoNumber(7),
        drawDate = LocalDate.of(2026, 2, 21),
    )
