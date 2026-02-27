package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.feature.manualadd.ManualAddViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ManualAddViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 이번주에_동일번호가_있으면_선택지를_노출한다() =
        runTest {
            val duplicate = ticket(id = 1L, numbers = listOf(1, 2, 3, 4, 5, 6))
            val repository = ManualAddFakeTicketRepository(initial = listOf(duplicate))
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(1, 2, 3, 4, 5, 6).forEach(viewModel::toggleNumber)
            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isFalse()
            assertThat(viewModel.uiState.value.error).isNull()
            assertThat(viewModel.uiState.value.duplicatePrompt?.duplicateGameCount).isEqualTo(1)
            assertThat(repository.savedBundles).isEmpty()
        }

    @Test
    fun 중복제외_저장을_선택하면_신규게임만_저장한다() =
        runTest {
            val duplicate = ticket(id = 1L, numbers = listOf(1, 2, 3, 4, 5, 6))
            val repository = ManualAddFakeTicketRepository(initial = listOf(duplicate))
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(1, 2, 3, 4, 5, 6).forEach(viewModel::toggleNumber)
            viewModel.addSelectedGame()
            viewModel.clear()
            listOf(7, 8, 9, 10, 11, 12).forEach(viewModel::toggleNumber)
            viewModel.addSelectedGame()

            viewModel.save()
            advanceUntilIdle()
            viewModel.saveExcludingDuplicates()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isTrue()
            assertThat(viewModel.uiState.value.savedGameCount).isEqualTo(1)
            assertThat(repository.savedBundles).hasSize(1)
            assertThat(repository.savedBundles.first().games.first().numbers.map { it.value })
                .containsExactly(7, 8, 9, 10, 11, 12)
        }

    @Test
    fun 중복포함_저장을_선택하면_중복게임도_저장한다() =
        runTest {
            val duplicate = ticket(id = 1L, numbers = listOf(1, 2, 3, 4, 5, 6))
            val repository = ManualAddFakeTicketRepository(initial = listOf(duplicate))
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(1, 2, 3, 4, 5, 6).forEach(viewModel::toggleNumber)
            viewModel.save()
            advanceUntilIdle()
            viewModel.saveIncludingDuplicates()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isTrue()
            assertThat(viewModel.uiState.value.savedGameCount).isEqualTo(1)
            assertThat(repository.savedBundles).hasSize(1)
            assertThat(repository.savedBundles.first().games.first().numbers.map { it.value })
                .containsExactly(1, 2, 3, 4, 5, 6)
        }

    @Test
    fun 저장실패시_초안을_유지하고_에러를_노출한다() =
        runTest {
            val repository = ManualAddFakeTicketRepository(initial = emptyList(), failOnSave = true)
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(7, 8, 9, 10, 11, 12).forEach(viewModel::toggleNumber)
            viewModel.addSelectedGame()

            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isFalse()
            assertThat(viewModel.uiState.value.error).isEqualTo("저장에 실패했습니다. 다시 시도해 주세요.")
            assertThat(viewModel.uiState.value.pendingGames).containsExactly(listOf(7, 8, 9, 10, 11, 12))
            assertThat(repository.savedBundles).isEmpty()
        }

    @Test
    fun 저장직후_실행취소를_누르면_직전티켓을_삭제한다() =
        runTest {
            val repository = ManualAddFakeTicketRepository(initial = emptyList())
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(7, 8, 9, 10, 11, 12).forEach(viewModel::toggleNumber)
            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.lastSavedTicketId).isNotNull()
            viewModel.undoLastSavedTicket()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.lastSavedTicketId).isNull()
            assertThat(viewModel.uiState.value.error).isEqualTo("직전 저장을 취소했습니다.")
            assertThat(repository.observeCurrentRoundSnapshot()).isEmpty()
        }

    @Test
    fun 이번주_동일번호가_없으면_정상_저장한다() =
        runTest {
            val existing = ticket(id = 1L, numbers = listOf(1, 2, 3, 4, 5, 6))
            val repository = ManualAddFakeTicketRepository(initial = listOf(existing))
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(7, 8, 9, 10, 11, 12).forEach(viewModel::toggleNumber)
            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isTrue()
            assertThat(viewModel.uiState.value.error).isNull()
            assertThat(repository.savedBundles).hasSize(1)
            assertThat(repository.savedBundles.first().games.first().numbers.map { it.value })
                .containsExactly(7, 8, 9, 10, 11, 12)
        }

    @Test
    fun 여러게임을_추가한_뒤_한번에_저장할_수_있다() =
        runTest {
            val repository = ManualAddFakeTicketRepository(initial = emptyList())
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(1, 2, 3, 4, 5, 6).forEach(viewModel::toggleNumber)
            viewModel.addSelectedGame()
            viewModel.clear()
            listOf(7, 8, 9, 10, 11, 12).forEach(viewModel::toggleNumber)
            viewModel.addSelectedGame()

            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isTrue()
            assertThat(viewModel.uiState.value.savedGameCount).isEqualTo(2)
            assertThat(repository.savedBundles).hasSize(1)
            assertThat(repository.savedBundles.first().games).hasSize(2)
            assertThat(repository.savedBundles.first().games[0].slot).isEqualTo(GameSlot.A)
            assertThat(repository.savedBundles.first().games[1].slot).isEqualTo(GameSlot.B)
        }

    @Test
    fun 같은번호_반복추가로_게임수를_늘릴_수_있다() =
        runTest {
            val repository = ManualAddFakeTicketRepository(initial = emptyList())
            val viewModel = ManualAddViewModel(ticketRepository = repository)

            listOf(1, 2, 3, 4, 5, 6).forEach(viewModel::toggleNumber)
            viewModel.setRepeatCount(5)
            viewModel.addSelectedGameRepeated()
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedBundles.first()
            assertThat(saved.games).hasSize(5)
            assertThat(saved.games.map { it.slot })
                .containsExactly(GameSlot.A, GameSlot.B, GameSlot.C, GameSlot.D, GameSlot.E)
                .inOrder()
            assertThat(saved.games.all { game -> game.numbers.map { it.value } == listOf(1, 2, 3, 4, 5, 6) }).isTrue()
        }
}

private fun ticket(
    id: Long,
    numbers: List<Int>,
): TicketBundle =
    TicketBundle(
        id = id,
        round = Round(number = 1200, drawDate = LocalDate.of(2026, 3, 7)),
        source = TicketSource.MANUAL,
        status = TicketStatus.PENDING,
        games =
            listOf(
                LottoGame(
                    slot = GameSlot.A,
                    numbers = numbers.map(::LottoNumber),
                ),
            ),
    )

private class ManualAddFakeTicketRepository(
    initial: List<TicketBundle>,
    private val failOnSave: Boolean = false,
) : TicketRepository {
    private val all = MutableStateFlow(initial)
    val savedBundles: MutableList<TicketBundle> = mutableListOf()

    override fun observeAllTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        all.map { list -> list.filter { it.round.number == round.number } }

    override suspend fun save(bundle: TicketBundle) {
        if (failOnSave) {
            throw IllegalStateException("save failed")
        }
        savedBundles.add(bundle)
        all.update { it + bundle.copy(id = (it.maxOfOrNull { t -> t.id } ?: 0L) + 1L) }
    }

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = all.value.maxByOrNull { it.id }

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) {
        all.update { current -> current.filterNot { it.id in ids } }
    }

    fun observeCurrentRoundSnapshot(): List<TicketBundle> = all.value
}
