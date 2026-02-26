package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.feature.manage.ManageSort
import com.weeklylotto.app.feature.manage.ManageTab
import com.weeklylotto.app.feature.manage.ManageViewModel
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
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ManageViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 선택번호를_보관함으로_이동하면_이번주목록에서_제외된다() =
        runTest {
            val currentRoundNumber = RoundEstimator.currentSalesRound(LocalDate.now())
            val currentRound =
                Round(
                    number = currentRoundNumber,
                    drawDate = LocalDate.now().plusDays(2),
                )
            val oldRound =
                Round(
                    number = (currentRoundNumber - 1).coerceAtLeast(1),
                    drawDate = LocalDate.now().minusDays(5),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(id = 1L, round = currentRound, status = TicketStatus.PENDING),
                            ticket(id = 2L, round = oldRound, status = TicketStatus.PENDING),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            assertThat(viewModel.filteredTickets().map { it.id }).containsExactly(1L)

            viewModel.toggleEditMode()
            viewModel.toggleSelection(1L)
            viewModel.openMoveSheet()
            assertThat(viewModel.uiState.value.isMoveSheetOpen).isTrue()

            viewModel.moveSelectedToVault()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.editMode).isFalse()
            assertThat(viewModel.uiState.value.selectedIds).isEmpty()
            assertThat(viewModel.filteredTickets().map { it.id }).isEmpty()

            viewModel.setTab(ManageTab.VAULT)
            val vaultItems = viewModel.filteredTickets()
            assertThat(vaultItems.map { it.id }).containsAtLeast(1L, 2L)
            assertThat(vaultItems.first { it.id == 1L }.status).isEqualTo(TicketStatus.SAVED)
        }

    @Test
    fun 선택이_없으면_이동시트를_열지않는다() {
        val repository = ManageFakeTicketRepository(tickets = emptyList())
        val viewModel = ManageViewModel(ticketRepository = repository)

        viewModel.openMoveSheet()

        assertThat(viewModel.uiState.value.isMoveSheetOpen).isFalse()
    }

    @Test
    fun 과거회차_티켓은_이번주로_복사된다() =
        runTest {
            val today = LocalDate.now()
            val currentRoundNumber = RoundEstimator.currentSalesRound(today)
            val oldRound =
                Round(
                    number = (currentRoundNumber - 2).coerceAtLeast(1),
                    drawDate = today.minusDays(10),
                )
            val oldTicket = ticket(id = 7L, round = oldRound, status = TicketStatus.SAVED)
            val repository = ManageFakeTicketRepository(tickets = listOf(oldTicket))
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            viewModel.copyTicketToCurrentRound(oldTicket.id)
            advanceUntilIdle()

            val currentRoundTickets =
                viewModel.uiState.value.tickets.filter { it.round.number == currentRoundNumber }

            assertThat(currentRoundTickets).hasSize(1)
            assertThat(currentRoundTickets.first().source).isEqualTo(TicketSource.MANUAL)
            assertThat(currentRoundTickets.first().status).isEqualTo(TicketStatus.PENDING)
            assertThat(currentRoundTickets.first().createdAt).isAtLeast(oldTicket.createdAt)
            assertThat(viewModel.uiState.value.feedbackMessage).isEqualTo("이번 주 번호로 복사했습니다.")
        }

    @Test
    fun 같은회차_티켓복사는_차단된다() =
        runTest {
            val today = LocalDate.now()
            val currentRound =
                Round(
                    number = RoundEstimator.currentSalesRound(today),
                    drawDate = RoundEstimator.nextDrawDate(today),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets = listOf(ticket(id = 11L, round = currentRound, status = TicketStatus.PENDING)),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            val beforeCount = viewModel.uiState.value.tickets.size

            viewModel.copyTicketToCurrentRound(11L)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.tickets).hasSize(beforeCount)
            assertThat(viewModel.uiState.value.feedbackMessage).isEqualTo("이미 이번 주 회차 번호입니다.")
        }

    @Test
    fun 동일번호가_이번주에_이미있으면_복사를_차단한다() =
        runTest {
            val today = LocalDate.now()
            val currentRound =
                Round(
                    number = RoundEstimator.currentSalesRound(today),
                    drawDate = RoundEstimator.nextDrawDate(today),
                )
            val oldRound =
                Round(
                    number = (currentRound.number - 3).coerceAtLeast(1),
                    drawDate = today.minusDays(21),
                )
            val sourceFromOldRound = ticket(id = 31L, round = oldRound, status = TicketStatus.SAVED)
            val sameNumbersInCurrentRound = ticket(id = 32L, round = currentRound, status = TicketStatus.PENDING)
            val repository =
                ManageFakeTicketRepository(
                    tickets = listOf(sourceFromOldRound, sameNumbersInCurrentRound),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            val beforeCount = viewModel.uiState.value.tickets.size

            viewModel.copyTicketToCurrentRound(sourceFromOldRound.id)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.tickets).hasSize(beforeCount)
            assertThat(viewModel.uiState.value.feedbackMessage).isEqualTo("이미 이번 주에 동일 번호가 있습니다.")
        }

    @Test
    fun 이번주탭은_보관상태를_제외한다() =
        runTest {
            val today = LocalDate.now()
            val currentRound =
                Round(
                    number = RoundEstimator.currentSalesRound(today),
                    drawDate = RoundEstimator.nextDrawDate(today),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(id = 41L, round = currentRound, status = TicketStatus.PENDING),
                            ticket(id = 42L, round = currentRound, status = TicketStatus.SAVED),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()

            val weekIds = viewModel.filteredTickets().map { it.id }
            assertThat(weekIds).containsExactly(41L)
        }

    @Test
    fun 스캔탭은_QR_등록건만_노출한다() =
        runTest {
            val today = LocalDate.now()
            val currentRound =
                Round(
                    number = RoundEstimator.currentSalesRound(today),
                    drawDate = RoundEstimator.nextDrawDate(today),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(
                                id = 51L,
                                round = currentRound,
                                status = TicketStatus.PENDING,
                                source = TicketSource.QR_SCAN,
                            ),
                            ticket(
                                id = 52L,
                                round = currentRound,
                                status = TicketStatus.PENDING,
                                source = TicketSource.GENERATED,
                            ),
                            ticket(
                                id = 53L,
                                round = currentRound,
                                status = TicketStatus.WIN,
                                source = TicketSource.MANUAL,
                            ),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            viewModel.setTab(ManageTab.SCAN)

            val scanIds = viewModel.filteredTickets().map { it.id }
            assertThat(scanIds).containsExactly(51L)
        }

    @Test
    fun 스캔요약은_총건수_이번주건수_최신회차를_반환한다() =
        runTest {
            val today = LocalDate.now()
            val currentRoundNumber = RoundEstimator.currentSalesRound(today)
            val currentRound =
                Round(
                    number = currentRoundNumber,
                    drawDate = RoundEstimator.nextDrawDate(today),
                )
            val oldRound =
                Round(
                    number = (currentRoundNumber - 2).coerceAtLeast(1),
                    drawDate = today.minusDays(14),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(
                                id = 61L,
                                round = currentRound,
                                status = TicketStatus.PENDING,
                                source = TicketSource.QR_SCAN,
                            ),
                            ticket(
                                id = 62L,
                                round = oldRound,
                                status = TicketStatus.WIN,
                                source = TicketSource.QR_SCAN,
                            ),
                            ticket(
                                id = 63L,
                                round = currentRound,
                                status = TicketStatus.PENDING,
                                source = TicketSource.GENERATED,
                            ),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            val summary = viewModel.scanSummary()

            assertThat(summary.totalCount).isEqualTo(2)
            assertThat(summary.currentRoundCount).isEqualTo(1)
            assertThat(summary.latestRound).isEqualTo(currentRoundNumber)
        }

    @Test
    fun WEEK탭으로_전환하면_SAVED_상태필터는_제거된다() =
        runTest {
            val repository = ManageFakeTicketRepository(tickets = emptyList())
            val viewModel = ManageViewModel(ticketRepository = repository)

            viewModel.toggleStatusFilter(TicketStatus.SAVED)
            viewModel.setTab(ManageTab.VAULT)
            viewModel.setTab(ManageTab.WEEK)

            assertThat(viewModel.uiState.value.filter.statuses).doesNotContain(TicketStatus.SAVED)
        }

    @Test
    fun 상태필터_초기화는_회차필터를_유지한다() =
        runTest {
            val repository = ManageFakeTicketRepository(tickets = emptyList())
            val viewModel = ManageViewModel(ticketRepository = repository)
            val targetRange = 1000..1010

            viewModel.setRoundRange(targetRange)
            viewModel.toggleStatusFilter(TicketStatus.WIN)
            viewModel.clearStatusFilters()

            assertThat(viewModel.uiState.value.filter.statuses).isEmpty()
            assertThat(viewModel.uiState.value.filter.roundRange).isEqualTo(targetRange)
        }

    @Test
    fun 회차필터_직접범위를_적용하면_해당구간만_노출한다() =
        runTest {
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(
                                id = 71L,
                                round = Round(1001, LocalDate.of(2025, 1, 4)),
                                status = TicketStatus.PENDING,
                            ),
                            ticket(
                                id = 72L,
                                round = Round(1005, LocalDate.of(2025, 2, 1)),
                                status = TicketStatus.WIN,
                            ),
                            ticket(
                                id = 73L,
                                round = Round(1011, LocalDate.of(2025, 3, 15)),
                                status = TicketStatus.LOSE,
                            ),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            viewModel.setTab(ManageTab.VAULT)
            viewModel.setRoundRange(1003..1010)

            val filteredIds = viewModel.filteredTickets().map { it.id }
            assertThat(filteredIds).containsExactly(72L)
        }

    @Test
    fun 전체필터_초기화는_상태와_회차를_모두_해제한다() =
        runTest {
            val repository = ManageFakeTicketRepository(tickets = emptyList())
            val viewModel = ManageViewModel(ticketRepository = repository)

            viewModel.toggleStatusFilter(TicketStatus.WIN)
            viewModel.setRoundRange(1000..1010)
            viewModel.clearFilter()

            assertThat(viewModel.uiState.value.filter.statuses).isEmpty()
            assertThat(viewModel.uiState.value.filter.roundRange).isNull()
        }

    @Test
    fun 기본정렬은_등록최신순이다() =
        runTest {
            val currentRound =
                Round(
                    number = RoundEstimator.currentSalesRound(LocalDate.now()),
                    drawDate = RoundEstimator.nextDrawDate(LocalDate.now()),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(id = 81L, round = currentRound, status = TicketStatus.PENDING).copy(
                                createdAt = Instant.parse("2026-02-01T10:00:00Z"),
                            ),
                            ticket(id = 82L, round = currentRound, status = TicketStatus.PENDING).copy(
                                createdAt = Instant.parse("2026-02-03T10:00:00Z"),
                            ),
                            ticket(id = 83L, round = currentRound, status = TicketStatus.PENDING).copy(
                                createdAt = Instant.parse("2026-02-02T10:00:00Z"),
                            ),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()

            assertThat(viewModel.filteredTickets().map { it.id }).containsExactly(82L, 83L, 81L).inOrder()
        }

    @Test
    fun 등록오래된순_정렬을_적용할수있다() =
        runTest {
            val currentRound =
                Round(
                    number = RoundEstimator.currentSalesRound(LocalDate.now()),
                    drawDate = RoundEstimator.nextDrawDate(LocalDate.now()),
                )
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(id = 91L, round = currentRound, status = TicketStatus.PENDING).copy(
                                createdAt = Instant.parse("2026-02-01T10:00:00Z"),
                            ),
                            ticket(id = 92L, round = currentRound, status = TicketStatus.PENDING).copy(
                                createdAt = Instant.parse("2026-02-03T10:00:00Z"),
                            ),
                            ticket(id = 93L, round = currentRound, status = TicketStatus.PENDING).copy(
                                createdAt = Instant.parse("2026-02-02T10:00:00Z"),
                            ),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            viewModel.setSort(ManageSort.OLDEST_CREATED)

            assertThat(viewModel.filteredTickets().map { it.id }).containsExactly(91L, 93L, 92L).inOrder()
        }

    @Test
    fun 회차순_정렬을_적용할수있다() =
        runTest {
            val repository =
                ManageFakeTicketRepository(
                    tickets =
                        listOf(
                            ticket(
                                id = 101L,
                                round = Round(1002, LocalDate.of(2025, 1, 11)),
                                status = TicketStatus.PENDING,
                            ).copy(createdAt = Instant.parse("2026-02-03T10:00:00Z")),
                            ticket(
                                id = 102L,
                                round = Round(1008, LocalDate.of(2025, 2, 22)),
                                status = TicketStatus.PENDING,
                            ).copy(createdAt = Instant.parse("2026-02-01T10:00:00Z")),
                            ticket(
                                id = 103L,
                                round = Round(1005, LocalDate.of(2025, 2, 1)),
                                status = TicketStatus.PENDING,
                            ).copy(createdAt = Instant.parse("2026-02-02T10:00:00Z")),
                        ),
                )
            val viewModel = ManageViewModel(ticketRepository = repository)

            advanceUntilIdle()
            viewModel.setTab(ManageTab.VAULT)
            viewModel.setSort(ManageSort.ROUND_DESC)

            assertThat(viewModel.filteredTickets().map { it.id }).containsExactly(102L, 103L, 101L).inOrder()
        }
}

private fun ticket(
    id: Long,
    round: Round,
    status: TicketStatus,
    source: TicketSource = TicketSource.GENERATED,
): TicketBundle =
    TicketBundle(
        id = id,
        round = round,
        createdAt = Instant.now(),
        games =
            listOf(
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(3, 11, 14, 22, 31, 45).map(::LottoNumber),
                ),
            ),
        source = source,
        status = status,
    )

private class ManageFakeTicketRepository(
    tickets: List<TicketBundle>,
) : TicketRepository {
    private val bundles = MutableStateFlow(tickets)
    private var nextId: Long = (tickets.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeAllTickets(): Flow<List<TicketBundle>> = bundles.asStateFlow()

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = bundles.asStateFlow()

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        bundles.map { list -> list.filter { it.round.number == round.number } }

    override suspend fun save(bundle: TicketBundle) {
        bundles.update { list ->
            val resolved =
                if (bundle.id > 0L) {
                    bundle
                } else {
                    bundle.copy(id = nextId++)
                }
            list + resolved
        }
    }

    override suspend fun update(bundle: TicketBundle) {
        bundles.update { list -> list.map { if (it.id == bundle.id) bundle else it } }
    }

    override suspend fun latest(): TicketBundle? = bundles.value.firstOrNull()

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) {
        bundles.update { list ->
            list.map { bundle ->
                if (bundle.id in ids) {
                    bundle.copy(status = status)
                } else {
                    bundle
                }
            }
        }
    }

    override suspend fun deleteByIds(ids: Set<Long>) {
        bundles.update { list -> list.filterNot { it.id in ids } }
    }
}
