package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.repository.DefaultResultEvaluator
import com.weeklylotto.app.data.repository.LocalTicketBackupService
import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsLogger
import com.weeklylotto.app.domain.service.AnalyticsParamKey
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
            val backupFile =
                Files.createTempDirectory(
                    "ticket-backup-test",
                ).resolve("tickets_backup_latest.json").toFile()
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
            val backupFile =
                Files.createTempDirectory(
                    "ticket-backup-restore",
                ).resolve("tickets_backup_latest.json").toFile()
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

            val targetRepository =
                BackupFakeTicketRepository(
                    listOf(backupTicket(id = 11L, round = 1202, numbers = listOf(40, 41, 42, 43, 44, 45))),
                )
            val service = LocalTicketBackupService(ticketRepository = targetRepository, backupFile = backupFile)

            val summary = service.restoreLatestBackup().getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(2)
            assertThat(targetRepository.observeSnapshot().map { it.round.number }).containsExactly(1201, 1201)
            assertThat(
                targetRepository.observeSnapshot().flatMap {
                        bundle ->
                    bundle.games
                }.map { game -> game.numbers.map { it.value } },
            )
                .containsExactly(listOf(1, 2, 3, 4, 5, 6), listOf(13, 14, 15, 16, 17, 18))
        }

    @Test
    fun 백업파일이_없으면_복원은_실패한다() =
        runTest {
            val repository = BackupFakeTicketRepository(emptyList())
            val backupFile =
                Files.createTempDirectory(
                    "ticket-backup-missing",
                ).resolve("tickets_backup_latest.json").toFile()
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
            val backupFile =
                Files.createTempDirectory(
                    "ticket-backup-integrity-pass",
                ).resolve("tickets_backup_latest.json").toFile()
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
            val backupFile =
                Files.createTempDirectory(
                    "ticket-backup-integrity-warn",
                ).resolve("tickets_backup_latest.json").toFile()
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

    @Test
    fun csv내보내기시_주차별구매와_당첨번호를_함께_생성한다() =
        runTest {
            val tickets =
                listOf(
                    backupTicket(
                        id = 10L,
                        round = 1201,
                        numbers = listOf(1, 2, 3, 4, 5, 6),
                        source = TicketSource.MANUAL,
                    ),
                    backupTicket(
                        id = 11L,
                        round = 1200,
                        numbers = listOf(7, 8, 9, 10, 11, 12),
                        source = TicketSource.GENERATED,
                    ),
                    backupTicket(
                        id = 12L,
                        round = 1201,
                        numbers = listOf(20, 21, 22, 23, 24, 25),
                        source = TicketSource.QR_SCAN,
                    ),
                )
            val repository = BackupFakeTicketRepository(tickets)
            val backupFile =
                Files.createTempDirectory(
                    "ticket-history-csv",
                ).resolve("tickets_backup_latest.json").toFile()
            val drawRepository =
                BackupCsvFakeDrawRepository(
                    rounds =
                        mapOf(
                            1201 to
                                DrawResult(
                                    round = Round(1201, LocalDate.of(2026, 3, 7)),
                                    mainNumbers = listOf(1, 3, 5, 7, 9, 11).map(::LottoNumber),
                                    bonus = LottoNumber(13),
                                    drawDate = LocalDate.of(2026, 3, 7),
                                ),
                        ),
                )
            val service =
                LocalTicketBackupService(
                    ticketRepository = repository,
                    backupFile = backupFile,
                    drawRepository = drawRepository,
                    resultEvaluator = DefaultResultEvaluator(),
                )

            val summary = service.exportTicketHistoryCsvForAi(startRound = null, endRound = null).getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(3)
            assertThat(summary.gameCount).isEqualTo(3)
            assertThat(summary.roundCount).isEqualTo(2)
            assertThat(summary.requestedStartRound).isNull()
            assertThat(summary.requestedEndRound).isNull()
            assertThat(summary.firstRoundNumber).isEqualTo(1200)
            assertThat(summary.lastRoundNumber).isEqualTo(1201)
            assertThat(summary.matchedDrawCount).isEqualTo(1)
            assertThat(summary.missingDrawCount).isEqualTo(1)
            assertThat(summary.missingRoundNumbers).containsExactly(1200)
            assertThat(summary.generatedGameCount).isEqualTo(1)
            assertThat(summary.manualGameCount).isEqualTo(1)
            assertThat(summary.qrGameCount).isEqualTo(1)
            assertThat(summary.winningGameCount).isEqualTo(1)
            assertThat(summary.totalExpectedPrizeAmount).isEqualTo(5000L)
            val csvText = java.io.File(summary.filePath).readText()
            assertThat(csvText).contains("round_number,round_draw_date,ticket_id,ticket_source")
            assertThat(csvText)
                .contains(
                    "draw_matched,matched_main_count,bonus_matched,draw_rank,expected_prize_amount",
                )
            assertThat(csvText).contains("1201,2026-03-07,10,MANUAL,PENDING")
            assertThat(csvText)
                .contains("1-2-3-4-5-6,1-3-5-7-9-11,13,Y,3,false,FIFTH,5000")
            assertThat(csvText).contains("1200,2026-03-07,11,GENERATED,PENDING")
            assertThat(csvText).contains("7-8-9-10-11-12,,,N,,,,")
            assertThat(csvText).contains("1201,2026-03-07,12,QR_SCAN,PENDING")
        }

    @Test
    fun csv내보내기시_회차필터를_적용한다() =
        runTest {
            val repository =
                BackupFakeTicketRepository(
                    listOf(
                        backupTicket(id = 20L, round = 1199, numbers = listOf(1, 2, 3, 4, 5, 6)),
                        backupTicket(id = 21L, round = 1200, numbers = listOf(7, 8, 9, 10, 11, 12)),
                        backupTicket(id = 22L, round = 1201, numbers = listOf(13, 14, 15, 16, 17, 18)),
                    ),
                )
            val backupFile =
                Files.createTempDirectory(
                    "ticket-history-csv-filter",
                ).resolve("tickets_backup_latest.json").toFile()
            val service =
                LocalTicketBackupService(
                    ticketRepository = repository,
                    backupFile = backupFile,
                    drawRepository = BackupCsvFakeDrawRepository(rounds = emptyMap()),
                    resultEvaluator = DefaultResultEvaluator(),
                )

            val summary = service.exportTicketHistoryCsvForAi(startRound = 1200, endRound = 1200).getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(1)
            assertThat(summary.gameCount).isEqualTo(1)
            assertThat(summary.roundCount).isEqualTo(1)
            assertThat(summary.requestedStartRound).isEqualTo(1200)
            assertThat(summary.requestedEndRound).isEqualTo(1200)
            assertThat(summary.firstRoundNumber).isEqualTo(1200)
            assertThat(summary.lastRoundNumber).isEqualTo(1200)
            val csvText = java.io.File(summary.filePath).readText()
            assertThat(csvText).contains("1200,2026-03-07,21,MANUAL,PENDING")
            assertThat(csvText).doesNotContain("1199,2026-03-07,20")
            assertThat(csvText).doesNotContain("1201,2026-03-07,22")
        }

    @Test
    fun csv내보내기시_회차필터결과가없으면_빈요약을_반환한다() =
        runTest {
            val repository =
                BackupFakeTicketRepository(
                    listOf(
                        backupTicket(id = 30L, round = 1200, numbers = listOf(1, 2, 3, 4, 5, 6)),
                        backupTicket(id = 31L, round = 1201, numbers = listOf(7, 8, 9, 10, 11, 12)),
                    ),
                )
            val backupFile =
                Files.createTempDirectory(
                    "ticket-history-csv-filter-empty",
                ).resolve("tickets_backup_latest.json").toFile()
            val service =
                LocalTicketBackupService(
                    ticketRepository = repository,
                    backupFile = backupFile,
                    drawRepository = BackupCsvFakeDrawRepository(rounds = emptyMap()),
                    resultEvaluator = DefaultResultEvaluator(),
                )

            val summary = service.exportTicketHistoryCsvForAi(startRound = 1300, endRound = 1301).getOrThrow()

            assertThat(summary.ticketCount).isEqualTo(0)
            assertThat(summary.gameCount).isEqualTo(0)
            assertThat(summary.roundCount).isEqualTo(0)
            assertThat(summary.requestedStartRound).isEqualTo(1300)
            assertThat(summary.requestedEndRound).isEqualTo(1301)
            assertThat(summary.firstRoundNumber).isNull()
            assertThat(summary.lastRoundNumber).isNull()
            val csvText = java.io.File(summary.filePath).readText()
            assertThat(csvText.lines().first()).contains("round_number,round_draw_date,ticket_id,ticket_source")
            assertThat(csvText).doesNotContain("1200,2026-03-07,30")
            assertThat(csvText).doesNotContain("1201,2026-03-07,31")
        }
}

private fun backupTicket(
    id: Long,
    round: Int,
    numbers: List<Int>,
    source: TicketSource = TicketSource.MANUAL,
): TicketBundle =
    TicketBundle(
        id = id,
        round = Round(number = round, drawDate = LocalDate.of(2026, 3, 7)),
        source = source,
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

private class BackupCsvFakeDrawRepository(
    private val rounds: Map<Int, DrawResult>,
) : DrawRepository {
    override suspend fun fetchLatest(): AppResult<DrawResult> =
        rounds
            .values
            .maxByOrNull { drawResult -> drawResult.round.number }
            ?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.StorageError("draw not found"))

    override suspend fun fetchByRound(round: Round): AppResult<DrawResult> =
        rounds[round.number]
            ?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.StorageError("draw not found for round=${round.number}"))
}
