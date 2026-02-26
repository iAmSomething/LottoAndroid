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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class NumberGeneratorViewModelTest {
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
) : NumberGenerator {
    override fun generateInitialGames(gameCount: Int): List<LottoGame> = initialGames

    override fun regenerateExceptLocked(games: List<LottoGame>): List<LottoGame> = games
}

private class FakeTicketRepository : TicketRepository {
    override fun observeAllTickets(): Flow<List<TicketBundle>> = flowOf(emptyList())

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = flowOf(emptyList())

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> = flowOf(emptyList())

    override suspend fun save(bundle: TicketBundle) = Unit

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = null

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}
