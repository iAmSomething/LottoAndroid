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
import com.weeklylotto.app.domain.service.ResultViewTracker
import com.weeklylotto.app.feature.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 최신회차_미확인_결과와_주간리포트를_계산한다() =
        runTest {
            val drawRound = Round(1212, LocalDate.of(2026, 2, 21))
            val draw =
                DrawResult(
                    round = drawRound,
                    mainNumbers = listOf(3, 12, 19, 24, 35, 41).map(::LottoNumber),
                    bonus = LottoNumber(7),
                    drawDate = LocalDate.of(2026, 2, 21),
                )
            val ticketRepository =
                HomeFakeTicketRepository(
                    listOf(
                        bundle(id = 1L, round = drawRound, games = 2),
                        bundle(id = 2L, round = Round(1211, LocalDate.of(2026, 2, 14)), games = 1),
                    ),
                )
            val viewModel =
                HomeViewModel(
                    ticketRepository = ticketRepository,
                    drawRepository = HomeFakeDrawRepository(draw),
                    resultEvaluator = HomeAlwaysFifthEvaluator,
                    resultViewTracker =
                        HomeFakeResultViewTracker(
                            lastViewedRound = 1211,
                            recentViewedRounds = listOf(1211, 1210),
                        ),
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.hasUnseenResult).isTrue()
            assertThat(state.unseenRound).isEqualTo(1212)
            assertThat(state.weeklyReport?.round).isEqualTo(1212)
            assertThat(state.weeklyReport?.totalGames).isEqualTo(2)
            assertThat(state.weeklyReport?.winningGames).isEqualTo(2)
            assertThat(state.weeklyReport?.totalPurchaseAmount).isEqualTo(2_000L)
            assertThat(state.weeklyReport?.totalWinningAmount).isEqualTo(10_000L)
            assertThat(state.weeklyReport?.winningRatePercent).isEqualTo(100)
            assertThat(state.weeklyReport?.resultViewed).isFalse()
            assertThat(state.routineHistory).hasSize(8)
            assertThat(state.routineHistory.first().round).isEqualTo(1212)
            assertThat(state.routineHistory.first().purchasedGames).isEqualTo(2)
            assertThat(state.routineHistory.first().resultViewed).isFalse()
            assertThat(state.routineHistory[1].round).isEqualTo(1211)
            assertThat(state.routineHistory[1].resultViewed).isTrue()
            assertThat(state.routineHistory[2].round).isEqualTo(1210)
            assertThat(state.routineHistory[2].resultViewed).isTrue()
        }

    @Test
    fun 최신회차를_이미_확인한_경우_주간리포트_확인상태를_true로_표시한다() =
        runTest {
            val drawRound = Round(1212, LocalDate.of(2026, 2, 21))
            val draw =
                DrawResult(
                    round = drawRound,
                    mainNumbers = listOf(3, 12, 19, 24, 35, 41).map(::LottoNumber),
                    bonus = LottoNumber(7),
                    drawDate = LocalDate.of(2026, 2, 21),
                )
            val viewModel =
                HomeViewModel(
                    ticketRepository = HomeFakeTicketRepository(listOf(bundle(id = 1L, round = drawRound, games = 1))),
                    drawRepository = HomeFakeDrawRepository(draw),
                    resultEvaluator = HomeAlwaysFifthEvaluator,
                    resultViewTracker = HomeFakeResultViewTracker(lastViewedRound = 1212),
                )

            advanceUntilIdle()

            assertThat(viewModel.uiState.value.weeklyReport?.resultViewed).isTrue()
        }
}

private class HomeFakeTicketRepository(
    initialTickets: List<TicketBundle>,
) : TicketRepository {
    private val tickets = MutableStateFlow(initialTickets)

    override fun observeAllTickets(): Flow<List<TicketBundle>> = tickets.asStateFlow()

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = tickets.asStateFlow()

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        tickets.map { list -> list.filter { it.round.number == round.number } }

    override suspend fun save(bundle: TicketBundle) = Unit

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = tickets.value.firstOrNull()

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}

private class HomeFakeDrawRepository(
    private val draw: DrawResult,
) : DrawRepository {
    override suspend fun fetchLatest(): AppResult<DrawResult> = AppResult.Success(draw)

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> = AppResult.Success(draw)
}

private class HomeFakeResultViewTracker(
    private val lastViewedRound: Int?,
    private val recentViewedRounds: List<Int> = emptyList(),
) : ResultViewTracker {
    override suspend fun loadLastViewedRound(): Int? = lastViewedRound

    override suspend fun loadRecentViewedRounds(limit: Int): List<Int> = recentViewedRounds.take(limit)

    override suspend fun markRoundViewed(roundNumber: Int) = Unit
}

private object HomeAlwaysFifthEvaluator : ResultEvaluator {
    override fun evaluate(
        game: LottoGame,
        draw: DrawResult,
    ): EvaluationResult =
        EvaluationResult(
            rank = DrawRank.FIFTH,
            matchedMainCount = 3,
            bonusMatched = false,
            highlightedNumbers = game.numbers.take(3).toSet(),
        )
}

private fun bundle(
    id: Long,
    round: Round,
    games: Int,
): TicketBundle =
    TicketBundle(
        id = id,
        round = round,
        source = TicketSource.GENERATED,
        games =
            (0 until games).map { index ->
                LottoGame(
                    slot = GameSlot.entries[index],
                    numbers = listOf(1, 2, 3, 4, 5, 6).map(::LottoNumber),
                )
            },
    )
