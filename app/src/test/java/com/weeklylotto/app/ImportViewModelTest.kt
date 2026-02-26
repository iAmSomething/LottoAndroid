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
import com.weeklylotto.app.feature.importticket.ImportViewModel
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
class ImportViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 가져오기_저장시_이번주_동일번호가_있으면_차단한다() =
        runTest {
            val duplicate = importTicket(id = 1L, numbers = listOf(3, 14, 25, 31, 38, 42))
            val repository = ImportFakeTicketRepository(initial = listOf(duplicate))
            val viewModel = ImportViewModel(ticketRepository = repository)

            viewModel.onInputChanged("3 14 25 31 38 42")
            viewModel.parse()
            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isFalse()
            assertThat(viewModel.uiState.value.error).isEqualTo("이미 이번 주에 동일 번호가 있습니다.")
            assertThat(repository.savedBundles).isEmpty()
        }

    @Test
    fun 가져오기_저장시_중복이_없으면_성공한다() =
        runTest {
            val existing = importTicket(id = 1L, numbers = listOf(1, 2, 3, 4, 5, 6))
            val repository = ImportFakeTicketRepository(initial = listOf(existing))
            val viewModel = ImportViewModel(ticketRepository = repository)

            viewModel.onInputChanged("7 8 9 10 11 12")
            viewModel.parse()
            viewModel.save()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.saved).isTrue()
            assertThat(viewModel.uiState.value.error).isNull()
            assertThat(repository.savedBundles).hasSize(1)
            assertThat(repository.savedBundles.first().games.first().numbers.map { it.value })
                .containsExactly(7, 8, 9, 10, 11, 12)
        }
}

private fun importTicket(
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

private class ImportFakeTicketRepository(
    initial: List<TicketBundle>,
) : TicketRepository {
    private val all = MutableStateFlow(initial)
    val savedBundles: MutableList<TicketBundle> = mutableListOf()

    override fun observeAllTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        all.map { list -> list.filter { it.round.number == round.number } }

    override suspend fun save(bundle: TicketBundle) {
        savedBundles.add(bundle)
        all.update { it + bundle.copy(id = (it.maxOfOrNull { t -> t.id } ?: 0L) + 1L) }
    }

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = all.value.firstOrNull()

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) = Unit
}
