package com.weeklylotto.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weeklylotto.app.data.local.WeeklyLottoDatabase
import com.weeklylotto.app.data.repository.RoomTicketRepository
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomTicketRepositoryIntegrationTest {
    private lateinit var database: WeeklyLottoDatabase
    private lateinit var repository: RoomTicketRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, WeeklyLottoDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        repository =
            RoomTicketRepository(
                ticketDao = database.ticketDao(),
                widgetRefreshScheduler =
                    object : WidgetRefreshScheduler {
                        override suspend fun refreshAll() {
                            // no-op for integration test
                        }
                    },
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun 회차별_티켓_저장후_조회된다() =
        runBlocking {
            val round = Round(number = 1200, drawDate = LocalDate.parse("2026-04-25"))
            val game =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(3, 14, 25, 31, 38, 42).map(::LottoNumber),
                )

            repository.save(
                TicketBundle(
                    round = round,
                    games = listOf(game),
                    source = TicketSource.GENERATED,
                ),
            )

            val stored = repository.observeTicketsByRound(round).first()
            assertEquals(1, stored.size)
            assertEquals(1, stored.first().games.size)
            assertEquals(round.number, stored.first().round.number)
        }

    @Test
    fun 동일회차_자동생성_저장은_최신값으로_덮어쓴다() =
        runBlocking {
            val round = Round(number = 1201, drawDate = LocalDate.parse("2026-05-02"))
            val firstGame =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(1, 2, 3, 4, 5, 6).map(::LottoNumber),
                )
            val secondGame =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(7, 8, 9, 10, 11, 12).map(::LottoNumber),
                )

            repository.save(
                TicketBundle(
                    round = round,
                    games = listOf(firstGame),
                    source = TicketSource.GENERATED,
                ),
            )
            repository.save(
                TicketBundle(
                    round = round,
                    games = listOf(secondGame),
                    source = TicketSource.GENERATED,
                ),
            )

            val stored = repository.observeTicketsByRound(round).first()
            assertEquals(1, stored.size)
            assertEquals(secondGame.numbers.map { it.value }, stored.first().games.first().numbers.map { it.value })
        }

    @Test
    fun 동일회차_QR저장은_누적된다() =
        runBlocking {
            val round = Round(number = 1202, drawDate = LocalDate.parse("2026-05-09"))
            val gameA =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(1, 12, 18, 24, 33, 41).map(::LottoNumber),
                )
            val gameB =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(4, 15, 19, 27, 35, 42).map(::LottoNumber),
                )

            repository.save(
                TicketBundle(
                    round = round,
                    games = listOf(gameA),
                    source = TicketSource.QR_SCAN,
                ),
            )
            repository.save(
                TicketBundle(
                    round = round,
                    games = listOf(gameB),
                    source = TicketSource.QR_SCAN,
                ),
            )

            val stored = repository.observeTicketsByRound(round).first()
            assertEquals(2, stored.size)
        }

    @Test
    fun 선택한_티켓상태를_보관함으로_일괄변경한다() =
        runBlocking {
            val round = Round(number = 1203, drawDate = LocalDate.parse("2026-05-16"))
            val game =
                LottoGame(
                    slot = GameSlot.A,
                    numbers = listOf(3, 11, 14, 22, 31, 45).map(::LottoNumber),
                )

            repository.save(
                TicketBundle(
                    round = round,
                    games = listOf(game),
                    source = TicketSource.MANUAL,
                    status = TicketStatus.PENDING,
                ),
            )

            val saved = repository.observeTicketsByRound(round).first().first()
            repository.updateStatusByIds(setOf(saved.id), TicketStatus.SAVED)

            val updated = repository.observeTicketsByRound(round).first().first()
            assertEquals(TicketStatus.SAVED, updated.status)
        }
}
