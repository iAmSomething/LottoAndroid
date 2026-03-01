package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.NumberGenerator
import com.weeklylotto.app.feature.generator.NumberGeneratorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NumberGeneratorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 수동입력_빈값이면_에러메시지를_노출한다() {
        val viewModel =
            NumberGeneratorViewModel(
                numberGenerator = FakeNumberGenerator(baseGames()),
                ticketRepository = FakeTicketRepository(),
            )

        viewModel.applyManualNumber(GameSlot.A, "")

        assertThat(viewModel.uiState.value.manualInputError).isEqualTo("번호를 입력해주세요.")
    }

    @Test
    fun 수동입력시_모든번호고정상태면_교체하지않고_에러를_노출한다() {
        val lockedGame =
            LottoGame(
                slot = GameSlot.A,
                numbers = listOf(1, 2, 3, 4, 5, 6).map(::LottoNumber),
                lockedNumbers = listOf(1, 2, 3, 4, 5, 6).map(::LottoNumber).toSet(),
                mode = GameMode.MANUAL,
            )
        val viewModel =
            NumberGeneratorViewModel(
                numberGenerator = FakeNumberGenerator(listOf(lockedGame)),
                ticketRepository = FakeTicketRepository(),
            )

        viewModel.applyManualNumber(GameSlot.A, "7")

        assertThat(viewModel.uiState.value.games.first().numbers.map { it.value })
            .containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(viewModel.uiState.value.manualInputError)
            .isEqualTo("해당 게임은 6개 번호가 모두 고정되어 교체할 수 없습니다.")
    }

    @Test
    fun 수동입력시_교체대상을_지정하면_해당번호가_교체된다() {
        val viewModel =
            NumberGeneratorViewModel(
                numberGenerator = FakeNumberGenerator(baseGames()),
                ticketRepository = FakeTicketRepository(),
            )

        viewModel.applyManualNumber(
            slot = GameSlot.A,
            rawInput = "40",
            replaceTargetNumber = 25,
        )

        val numbers = viewModel.uiState.value.games.first().numbers.map { it.value }
        assertThat(numbers).containsExactly(1, 7, 13, 19, 31, 40)
        assertThat(viewModel.uiState.value.manualInputError).isNull()
    }

    @Test
    fun 수동입력시_잠금번호를_교체대상으로_지정하면_에러를_노출한다() {
        val lockedGame =
            LottoGame(
                slot = GameSlot.A,
                numbers = listOf(1, 7, 13, 19, 25, 31).map(::LottoNumber),
                lockedNumbers = setOf(LottoNumber(25)),
                mode = GameMode.SEMI_AUTO,
            )
        val viewModel =
            NumberGeneratorViewModel(
                numberGenerator = FakeNumberGenerator(listOf(lockedGame)),
                ticketRepository = FakeTicketRepository(),
            )

        viewModel.applyManualNumber(
            slot = GameSlot.A,
            rawInput = "40",
            replaceTargetNumber = 25,
        )

        assertThat(viewModel.uiState.value.games.first().numbers.map { it.value })
            .containsExactly(1, 7, 13, 19, 25, 31)
        assertThat(viewModel.uiState.value.manualInputError)
            .isEqualTo("잠금되지 않은 번호를 교체 대상으로 선택해주세요.")
    }

    @Test
    fun 원탭_생성저장은_재생성결과를_저장한다() =
        runTest {
            val regeneratedGame =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(2, 8, 14, 20, 26, 32).map(::LottoNumber),
                )
            val repository = FakeTicketRepository()
            val generator =
                FakeNumberGenerator(
                    initialGames = baseGames(),
                    regeneratedGames = listOf(regeneratedGame),
                )
            val viewModel =
                NumberGeneratorViewModel(
                    numberGenerator = generator,
                    ticketRepository = repository,
                )

            viewModel.regenerateAndSaveAsWeeklyTicket()
            advanceUntilIdle()

            assertThat(generator.regenerateCallCount).isEqualTo(1)
            assertThat(repository.savedBundles).hasSize(1)
            assertThat(repository.savedBundles.first().games.first().numbers.map { it.value })
                .containsExactly(2, 8, 14, 20, 26, 32)
            assertThat(viewModel.uiState.value.toastMessage).isEqualTo("잠금 번호 기준으로 새 번호를 생성해 저장했습니다.")
        }

    private fun baseGames(): List<LottoGame> =
        listOf(
            LottoGame(
                slot = GameSlot.A,
                numbers = listOf(1, 7, 13, 19, 25, 31).map(::LottoNumber),
            ),
        )
}

private class FakeNumberGenerator(
    private val initialGames: List<LottoGame>,
    private val regeneratedGames: List<LottoGame> = initialGames,
) : NumberGenerator {
    var regenerateCallCount: Int = 0

    override fun generateInitialGames(gameCount: Int): List<LottoGame> = initialGames

    override fun regenerateExceptLocked(games: List<LottoGame>): List<LottoGame> {
        regenerateCallCount += 1
        return regeneratedGames
    }
}

private class FakeTicketRepository : TicketRepository {
    val savedBundles: MutableList<TicketBundle> = mutableListOf()

    override fun observeAllTickets(): Flow<List<TicketBundle>> = flowOf(emptyList())

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = flowOf(emptyList())

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> = flowOf(emptyList())

    override suspend fun save(bundle: TicketBundle) {
        savedBundles += bundle
    }

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = null

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}
