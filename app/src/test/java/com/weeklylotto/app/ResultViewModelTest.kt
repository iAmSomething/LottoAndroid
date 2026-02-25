package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.EvaluationResult
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.ResultEvaluator
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
}

private class ResultViewModelFakeDrawRepository(
    private val latestQueue: ArrayDeque<AppResult<DrawResult>>,
) : DrawRepository {
    var fetchLatestCount: Int = 0

    override suspend fun fetchLatest(): AppResult<DrawResult> {
        fetchLatestCount += 1
        return latestQueue.removeFirstOrNull()
            ?: AppResult.Failure(AppError.NetworkError("no more queued response"))
    }

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> = fetchLatest()
}

private class ResultViewModelFakeTicketRepository : TicketRepository {
    override fun observeAllTickets(): Flow<List<TicketBundle>> = flowOf(emptyList())

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = flowOf(emptyList())

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> = flowOf(emptyList())

    override suspend fun save(bundle: TicketBundle) = Unit

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = null

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}

private class ResultViewModelFakeResultEvaluator : ResultEvaluator {
    override fun evaluate(
        game: LottoGame,
        draw: DrawResult,
    ): EvaluationResult =
        EvaluationResult(
            rank = DrawRank.NONE,
            matchedMainCount = 0,
            bonusMatched = false,
            highlightedNumbers = emptySet(),
        )
}

private fun sampleDraw(roundNumber: Int): DrawResult =
    DrawResult(
        round = Round(roundNumber, LocalDate.of(2026, 2, 21)),
        mainNumbers = listOf(3, 12, 19, 24, 35, 41).map(::LottoNumber),
        bonus = LottoNumber(7),
        drawDate = LocalDate.of(2026, 2, 21),
    )
