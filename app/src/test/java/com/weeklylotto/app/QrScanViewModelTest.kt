package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.qr.QrTicketParser
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.feature.qr.QrScanViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class QrScanViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 지원형식실패시_가이드타이틀을_지원형식확인으로_노출한다() =
        runTest {
            val viewModel =
                QrScanViewModel(
                    parser = QrTicketParser(),
                    ticketRepository = QrFakeTicketRepository(),
                )

            viewModel.parseForConfirm("https://example.com?foo=bar")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.consecutiveFailureCount).isEqualTo(1)
            assertThat(state.failureGuideTitle).isEqualTo("지원 형식 확인")
            assertThat(state.failureGuideMessage).contains("동행복권 QR")
        }

    @Test
    fun payload누락실패시_가이드타이틀을_정보누락으로_노출한다() =
        runTest {
            val viewModel =
                QrScanViewModel(
                    parser = QrTicketParser(),
                    ticketRepository = QrFakeTicketRepository(),
                )

            viewModel.parseForConfirm("https://example.com?drwNo=1100")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.consecutiveFailureCount).isEqualTo(1)
            assertThat(state.failureGuideTitle).isEqualTo("QR 정보 누락")
        }

    @Test
    fun 파싱성공후_번호형식오류면_번호형식가이드를_노출한다() =
        runTest {
            val viewModel =
                QrScanViewModel(
                    parser = QrTicketParser(),
                    ticketRepository = QrFakeTicketRepository(),
                )

            viewModel.parseForConfirm("https://example.com?drwNo=1100&numbers=1,1,2,3,4,5")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.consecutiveFailureCount).isEqualTo(1)
            assertThat(state.failureGuideTitle).isEqualTo("번호 형식 오류")
            assertThat(state.pendingScan).isNull()
        }

    @Test
    fun 성공파싱시_실패가이드가_초기화된다() =
        runTest {
            val viewModel =
                QrScanViewModel(
                    parser = QrTicketParser(),
                    ticketRepository = QrFakeTicketRepository(),
                )

            viewModel.parseForConfirm("https://example.com?foo=bar")
            advanceUntilIdle()
            viewModel.parseForConfirm("https://example.com?drwNo=1100&numbers=1,2,3,4,5,6")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.consecutiveFailureCount).isEqualTo(0)
            assertThat(state.failureGuideTitle).isNull()
            assertThat(state.failureGuideMessage).isNull()
            assertThat(state.pendingScan).isNotNull()
        }
}

private class QrFakeTicketRepository : TicketRepository {
    private val all = MutableStateFlow<List<TicketBundle>>(emptyList())

    override fun observeAllTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        all.map { list -> list.filter { ticket -> ticket.round.number == round.number } }

    override suspend fun save(bundle: TicketBundle) {
        val nextId = (all.value.maxOfOrNull { it.id } ?: 0L) + 1L
        all.update { current -> current + bundle.copy(id = nextId) }
    }

    override suspend fun update(bundle: TicketBundle) = Unit

    override suspend fun latest(): TicketBundle? = all.value.maxByOrNull { it.id }

    override suspend fun updateStatusByIds(
        ids: Set<Long>,
        status: TicketStatus,
    ) = Unit

    override suspend fun deleteByIds(ids: Set<Long>) {
        all.update { current -> current.filterNot { ticket -> ticket.id in ids } }
    }
}
