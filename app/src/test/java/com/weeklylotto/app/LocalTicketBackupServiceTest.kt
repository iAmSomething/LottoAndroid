package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.repository.LocalTicketBackupService
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsLogger
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDate

class LocalTicketBackupServiceTest {
    @Test
    fun 백업시_티켓json파일을_생성한다() =
        runTest {
            val repository =
                BackupFakeTicketRepository(
                    listOf(
                        backupTicket(id = 1L, round = 1201, numbers = listOf(1, 2, 3, 4, 5, 6)),
                        backupTicket(id = 2L, round = 1200, numbers = listOf(7, 8, 9, 10, 11, 12)),
                    ),
                )
            val backupFile = Files.createTempDirectory("ticket-backup-test").resolve("tickets_backup_latest.json").toFile()
            val service = LocalTicketBackupService(ticketRepository = repository, backupFile = backupFile)

            val summary = service.backupCurrentTickets().getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(2)
            assertThat(summary.gameCount).isEqualTo(2)
            assertThat(backupFile.exists()).isTrue()
            assertThat(backupFile.readText()).contains("\"schemaVersion\":1")
        }

    @Test
    fun 복원시_기존티켓을_교체하고_백업데이터를_저장한다() =
        runTest {
            val backupFile = Files.createTempDirectory("ticket-backup-restore").resolve("tickets_backup_latest.json").toFile()
            val sourceRepository =
                BackupFakeTicketRepository(
                    listOf(
                        backupTicket(id = 1L, round = 1201, numbers = listOf(1, 2, 3, 4, 5, 6)),
                        backupTicket(id = 2L, round = 1201, numbers = listOf(13, 14, 15, 16, 17, 18)),
                    ),
                )
            LocalTicketBackupService(ticketRepository = sourceRepository, backupFile = backupFile)
                .backupCurrentTickets()
                .getOrThrow()

            val targetRepository = BackupFakeTicketRepository(listOf(backupTicket(id = 11L, round = 1202, numbers = listOf(40, 41, 42, 43, 44, 45))))
            val service = LocalTicketBackupService(ticketRepository = targetRepository, backupFile = backupFile)

            val summary = service.restoreLatestBackup().getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(2)
            assertThat(targetRepository.observeSnapshot().map { it.round.number }).containsExactly(1201, 1201)
            assertThat(targetRepository.observeSnapshot().flatMap { bundle -> bundle.games }.map { game -> game.numbers.map { it.value } })
                .containsExactly(listOf(1, 2, 3, 4, 5, 6), listOf(13, 14, 15, 16, 17, 18))
        }

    @Test
    fun 백업파일이_없으면_복원은_실패한다() =
        runTest {
            val repository = BackupFakeTicketRepository(emptyList())
            val backupFile = Files.createTempDirectory("ticket-backup-missing").resolve("tickets_backup_latest.json").toFile()
            val service = LocalTicketBackupService(ticketRepository = repository, backupFile = backupFile)

            val result = service.restoreLatestBackup()

            assertThat(result.isFailure).isTrue()
        }

    @Test
    fun 무결성점검시_정상백업은_문제없음으로_요약한다() =
        runTest {
            val repository =
                BackupFakeTicketRepository(
                    listOf(
                        backupTicket(id = 1L, round = 1201, numbers = listOf(1, 2, 3, 4, 5, 6)),
                        backupTicket(id = 2L, round = 1200, numbers = listOf(7, 8, 9, 10, 11, 12)),
                    ),
                )
            val backupFile = Files.createTempDirectory("ticket-backup-integrity-pass").resolve("tickets_backup_latest.json").toFile()
            val analytics = RecordingAnalyticsLogger()
            val service =
                LocalTicketBackupService(
                    ticketRepository = repository,
                    backupFile = backupFile,
                    analyticsLogger = analytics,
                )
            service.backupCurrentTickets().getOrThrow()

            val summary = service.verifyLatestBackupIntegrity().getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(2)
            assertThat(summary.gameCount).isEqualTo(2)
            assertThat(summary.issueCount).isEqualTo(0)
            assertThat(summary.duplicateTicketCount).isEqualTo(0)
            assertThat(summary.invalidGameCount).isEqualTo(0)
            assertThat(summary.brokenTicketCount).isEqualTo(0)
            val event = analytics.events.single()
            assertThat(event.first).isEqualTo(AnalyticsEvent.OPS_DATA_INTEGRITY)
            assertThat(event.second[AnalyticsParamKey.STATUS]).isEqualTo("pass")
            assertThat(event.second[AnalyticsParamKey.ISSUE_COUNT]).isEqualTo("0")
        }

    @Test
    fun 무결성점검시_중복게임오류깨진레코드를_집계한다() =
        runTest {
            val backupFile = Files.createTempDirectory("ticket-backup-integrity-warn").resolve("tickets_backup_latest.json").toFile()
            val analytics = RecordingAnalyticsLogger()
            val service =
                LocalTicketBackupService(
                    ticketRepository = BackupFakeTicketRepository(emptyList()),
                    backupFile = backupFile,
                    analyticsLogger = analytics,
                )
            backupFile.writeText(
                """
                {
                  "schemaVersion": 1,
                  "exportedAt": "2026-02-27T00:00:00Z",
                  "tickets": [
                    {
                      "roundNumber": 1201,
                      "drawDate": "2026-03-07",
                      "source": "MANUAL",
                      "status": "PENDING",
                      "createdAt": "2026-02-27T00:00:00Z",
                      "games": [{"numbers": [1, 2, 3, 4, 5, 6]}]
                    },
                    {
                      "roundNumber": 1201,
                      "drawDate": "2026-03-07",
                      "source": "MANUAL",
                      "status": "PENDING",
                      "createdAt": "2026-02-27T00:00:00Z",
                      "games": [{"numbers": [1, 2, 3, 4, 5, 6]}]
                    },
                    {
                      "roundNumber": 1201,
                      "drawDate": "2026-03-07",
                      "source": "MANUAL",
                      "status": "PENDING",
                      "createdAt": "2026-02-27T01:00:00Z",
                      "games": [{"numbers": [1, 1, 3, 4, 5, 6]}]
                    },
                    {
                      "drawDate": "2026-03-07",
                      "source": "MANUAL",
                      "status": "PENDING",
                      "createdAt": "2026-02-27T01:00:00Z",
                      "games": [{"numbers": [7, 8, 9, 10, 11, 12]}]
                    }
                  ]
                }
                """.trimIndent(),
            )

            val summary = service.verifyLatestBackupIntegrity().getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(4)
            assertThat(summary.gameCount).isEqualTo(2)
            assertThat(summary.duplicateTicketCount).isEqualTo(1)
            assertThat(summary.invalidGameCount).isEqualTo(1)
            assertThat(summary.brokenTicketCount).isEqualTo(1)
            assertThat(summary.issueCount).isEqualTo(3)
            val event = analytics.events.single()
            assertThat(event.first).isEqualTo(AnalyticsEvent.OPS_DATA_INTEGRITY)
            assertThat(event.second[AnalyticsParamKey.STATUS]).isEqualTo("warn")
            assertThat(event.second[AnalyticsParamKey.ISSUE_COUNT]).isEqualTo("3")
        }
}

private fun backupTicket(
    id: Long,
    round: Int,
    numbers: List<Int>,
): TicketBundle =
    TicketBundle(
        id = id,
        round = Round(number = round, drawDate = LocalDate.of(2026, 3, 7)),
        source = TicketSource.MANUAL,
        status = TicketStatus.PENDING,
        createdAt = Instant.parse("2026-02-27T00:00:00Z"),
        games =
            listOf(
                LottoGame(
                    slot = GameSlot.A,
                    numbers = numbers.map(::LottoNumber),
                    mode = GameMode.MANUAL,
                ),
            ),
    )

private class BackupFakeTicketRepository(
    initial: List<TicketBundle>,
) : TicketRepository {
    private val all = MutableStateFlow(initial)

    override fun observeAllTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeCurrentRoundTickets(): Flow<List<TicketBundle>> = all.asStateFlow()

    override fun observeTicketsByRound(round: Round): Flow<List<TicketBundle>> =
        all.map { list -> list.filter { it.round.number == round.number } }

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
        all.update { current -> current.filterNot { it.id in ids } }
    }

    fun observeSnapshot(): List<TicketBundle> = all.value
}

private class RecordingAnalyticsLogger : AnalyticsLogger {
    val events: MutableList<Pair<String, Map<String, String>>> = mutableListOf()

    override fun log(
        event: String,
        params: Map<String, String>,
    ) {
        events += event to params
    }
}
