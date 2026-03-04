package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.ReminderConfig
import com.weeklylotto.app.domain.service.MotionPreferenceStore
import com.weeklylotto.app.domain.service.ReminderConfigStore
import com.weeklylotto.app.domain.service.ReminderScheduler
import com.weeklylotto.app.domain.service.TicketBackupIntegritySummary
import com.weeklylotto.app.domain.service.TicketBackupService
import com.weeklylotto.app.domain.service.TicketBackupSummary
import com.weeklylotto.app.domain.service.TicketHistoryCsvSummary
import com.weeklylotto.app.feature.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 금요일저녁_프리셋과_저장시_store_scheduler가_호출된다() =
        runTest {
            val initial = ReminderConfig(enabled = false)
            val store = FakeReminderConfigStore(initial)
            val scheduler = FakeReminderScheduler()
            val motionStore = FakeMotionPreferenceStore()
            val viewModel = SettingsViewModel(store, scheduler, motionStore)

            advanceUntilIdle()
            viewModel.useFridayEveningSchedule()
            viewModel.setEnabled(true)
            viewModel.saveSchedule()
            advanceUntilIdle()

            assertThat(store.saved).hasSize(1)
            assertThat(scheduler.scheduled).hasSize(1)
            val saved = store.saved.first()
            assertThat(saved.purchaseReminderDay).isEqualTo(DayOfWeek.FRIDAY)
            assertThat(saved.purchaseReminderTime).isEqualTo(LocalTime.of(18, 30))
            assertThat(saved.enabled).isTrue()
            assertThat(viewModel.uiState.value.message).isEqualTo("알림 설정을 저장했습니다.")
        }

    @Test
    fun 기본값_프리셋은_토요일_기본시간으로_복원된다() =
        runTest {
            val initial =
                ReminderConfig(purchaseReminderDay = DayOfWeek.MONDAY, purchaseReminderTime = LocalTime.of(9, 0))
            val viewModel =
                SettingsViewModel(
                    FakeReminderConfigStore(initial),
                    FakeReminderScheduler(),
                    FakeMotionPreferenceStore(),
                )

            advanceUntilIdle()
            viewModel.useDefaultSchedule()

            val config = viewModel.uiState.value.config
            assertThat(config.purchaseReminderDay).isEqualTo(DayOfWeek.SATURDAY)
            assertThat(config.purchaseReminderTime).isEqualTo(LocalTime.of(15, 0))
            assertThat(config.resultReminderTime).isEqualTo(LocalTime.of(21, 0))
        }

    @Test
    fun 모션축소_토글시_store에_즉시_저장된다() =
        runTest {
            val motionStore = FakeMotionPreferenceStore(initial = false)
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = motionStore,
                )

            advanceUntilIdle()
            viewModel.setReduceMotionEnabled(true)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.reduceMotionEnabled).isTrue()
            assertThat(motionStore.savedValues).containsExactly(true)
        }

    @Test
    fun 백업성공시_성공메시지를_노출한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    backupResult = Result.success(TicketBackupSummary(2, 4, "tickets_backup_latest.json")),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.backupTickets()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message).isEqualTo("백업 파일 생성 완료 (2건, 4게임)")
        }

    @Test
    fun 복원실패시_실패메시지를_노출한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    restoreResult = Result.failure(IllegalStateException("missing")),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.restoreTicketsFromBackup()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message).isEqualTo("백업 복원에 실패했습니다.")
        }

    @Test
    fun 무결성점검_문제없음이면_정상메시지를_노출한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    integrityResult =
                        Result.success(
                            TicketBackupIntegritySummary(
                                ticketCount = 2,
                                gameCount = 3,
                                duplicateTicketCount = 0,
                                invalidGameCount = 0,
                                brokenTicketCount = 0,
                                issueCount = 0,
                                fileName = "tickets_backup_latest.json",
                            ),
                        ),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.verifyBackupIntegrity()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message).isEqualTo("무결성 점검 완료 (문제 없음: 2건)")
        }

    @Test
    fun 무결성점검_문제발견이면_요약메시지를_노출한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    integrityResult =
                        Result.success(
                            TicketBackupIntegritySummary(
                                ticketCount = 4,
                                gameCount = 6,
                                duplicateTicketCount = 1,
                                invalidGameCount = 2,
                                brokenTicketCount = 1,
                                issueCount = 4,
                                fileName = "tickets_backup_latest.json",
                            ),
                        ),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.verifyBackupIntegrity()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message)
                .isEqualTo("무결성 점검 완료 (문제 4건: 중복 1, 게임오류 2, 레코드오류 1)")
        }

    @Test
    fun csv내보내기_성공시_공유경로와_메시지를_노출한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    csvExportResult =
                        Result.success(
                            TicketHistoryCsvSummary(
                                ticketCount = 3,
                                gameCount = 12,
                                roundCount = 2,
                                requestedStartRound = null,
                                requestedEndRound = null,
                                firstRoundNumber = 1200,
                                lastRoundNumber = 1201,
                                matchedDrawCount = 2,
                                missingDrawCount = 0,
                                missingRoundNumbers = emptyList(),
                                generatedGameCount = 5,
                                manualGameCount = 4,
                                qrGameCount = 3,
                                winningGameCount = 1,
                                totalExpectedPrizeAmount = 5000L,
                                fileName = "tickets_history_with_draw_latest.csv",
                                filePath = "/tmp/tickets_history_with_draw_latest.csv",
                            ),
                        ),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message)
                .isEqualTo("CSV 생성 완료 (2회차, 요청 필터 전체, 회차 범위 1200~1201회, 3건, 12게임, 당첨번호 포함 2회차, 당첨게임 1개, 예상당첨금 5000원)")
            assertThat(viewModel.uiState.value.csvShareRequest?.filePath)
                .isEqualTo("/tmp/tickets_history_with_draw_latest.csv")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("로또 주차별 구매/당첨 CSV 분석 요청")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 요청 필터: 전체")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .doesNotContain("- 필터 충족률")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 회차 범위: 1200~1201회")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .doesNotContain("필터 반영: 요청 범위 대비 실제 데이터 포함 회차는")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 출처별 게임 수: 자동 5, 수동 4, QR 3")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 데이터 신뢰도: 100% (당첨번호 매칭 회차 기준)")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("CSV 스키마 가이드:")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- draw_rank/expected_prize_amount: 게임별 평가 등수/예상 당첨금")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 당첨 게임 수: 1")
        }

    @Test
    fun csv내보내기_회차범위를_서비스에_전달한다() =
        runTest {
            val backupService = FakeTicketBackupService()
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi(startRound = 1200, endRound = 1201)
            advanceUntilIdle()

            assertThat(backupService.lastExportStartRound).isEqualTo(1200)
            assertThat(backupService.lastExportEndRound).isEqualTo(1201)
            assertThat(backupService.exportCallCount).isEqualTo(1)
        }

    @Test
    fun csv내보내기_역전된회차범위면_실패메시지를_노출하고_요청하지않는다() =
        runTest {
            val backupService = FakeTicketBackupService()
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi(startRound = 1203, endRound = 1201)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message).isEqualTo("CSV 회차 범위가 올바르지 않습니다.")
            assertThat(viewModel.uiState.value.csvShareRequest).isNull()
            assertThat(backupService.exportCallCount).isEqualTo(0)
        }

    @Test
    fun csv내보내기_대상데이터가없으면_공유를_생성하지않는다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    csvExportResult =
                        Result.success(
                            TicketHistoryCsvSummary(
                                ticketCount = 0,
                                gameCount = 0,
                                roundCount = 0,
                                requestedStartRound = 1300,
                                requestedEndRound = 1301,
                                firstRoundNumber = null,
                                lastRoundNumber = null,
                                matchedDrawCount = 0,
                                missingDrawCount = 0,
                                missingRoundNumbers = emptyList(),
                                generatedGameCount = 0,
                                manualGameCount = 0,
                                qrGameCount = 0,
                                winningGameCount = 0,
                                totalExpectedPrizeAmount = 0L,
                                fileName = "tickets_history_with_draw_latest.csv",
                                filePath = "/tmp/tickets_history_with_draw_latest.csv",
                            ),
                        ),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi(startRound = 1300, endRound = 1301)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message).isEqualTo("선택한 회차 범위에 내보낼 데이터가 없습니다.")
            assertThat(viewModel.uiState.value.csvShareRequest).isNull()
            assertThat(backupService.exportCallCount).isEqualTo(1)
        }

    @Test
    fun csv내보내기_실패시_실패메시지를_노출한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    csvExportResult = Result.failure(IllegalStateException("csv failed")),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.message).isEqualTo("CSV 생성에 실패했습니다.")
            assertThat(viewModel.uiState.value.csvShareRequest).isNull()
        }

    @Test
    fun csv내보내기_당첨번호누락회차가_있으면_프롬프트에_경고를_포함한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    csvExportResult =
                        Result.success(
                            TicketHistoryCsvSummary(
                                ticketCount = 4,
                                gameCount = 8,
                                roundCount = 3,
                                requestedStartRound = 1201,
                                requestedEndRound = 1203,
                                firstRoundNumber = 1201,
                                lastRoundNumber = 1203,
                                matchedDrawCount = 2,
                                missingDrawCount = 1,
                                missingRoundNumbers = listOf(1202),
                                generatedGameCount = 2,
                                manualGameCount = 5,
                                qrGameCount = 1,
                                winningGameCount = 0,
                                totalExpectedPrizeAmount = 0L,
                                fileName = "tickets_history_with_draw_latest.csv",
                                filePath = "/tmp/tickets_history_with_draw_latest.csv",
                            ),
                        ),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 경고: 당첨번호가 없는 회차 1개 포함")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 누락 회차 번호: 1202")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 요청 필터: 1201~1203회")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 필터 충족률 3/3회차 (100%)")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 회차 범위: 1201~1203회")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .doesNotContain("필터 반영: 요청 범위 대비 실제 데이터 포함 회차는")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 데이터 신뢰도: 66% (당첨번호 매칭 회차 기준)")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 주의: 누락 회차가 있어 결과 해석 시 보수적으로 판단해줘.")
        }

    @Test
    fun csv내보내기_요청필터와_실제회차가_다르면_보정안내를_포함한다() =
        runTest {
            val backupService =
                FakeTicketBackupService(
                    csvExportResult =
                        Result.success(
                            TicketHistoryCsvSummary(
                                ticketCount = 2,
                                gameCount = 6,
                                roundCount = 2,
                                requestedStartRound = 1200,
                                requestedEndRound = 1205,
                                firstRoundNumber = 1202,
                                lastRoundNumber = 1204,
                                matchedDrawCount = 1,
                                missingDrawCount = 1,
                                missingRoundNumbers = listOf(1204),
                                generatedGameCount = 2,
                                manualGameCount = 4,
                                qrGameCount = 0,
                                winningGameCount = 0,
                                totalExpectedPrizeAmount = 0L,
                                fileName = "tickets_history_with_draw_latest.csv",
                                filePath = "/tmp/tickets_history_with_draw_latest.csv",
                            ),
                        ),
                )
            val viewModel =
                SettingsViewModel(
                    reminderConfigStore = FakeReminderConfigStore(ReminderConfig()),
                    reminderScheduler = FakeReminderScheduler(),
                    motionPreferenceStore = FakeMotionPreferenceStore(),
                    ticketBackupService = backupService,
                )

            advanceUntilIdle()
            viewModel.exportTicketHistoryCsvForAi(startRound = 1200, endRound = 1205)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 요청 필터: 1200~1205회")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 필터 충족률 2/6회차 (33%)")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 회차 범위: 1202~1204회")
            assertThat(viewModel.uiState.value.csvShareRequest?.shareText)
                .contains("- 필터 반영: 요청 범위 대비 실제 데이터 포함 회차는 1202~1204회")
            assertThat(viewModel.uiState.value.message)
                .contains("필터 충족률 2/6회차 (33%)")
        }
}

private class FakeReminderConfigStore(
    private val loaded: ReminderConfig,
) : ReminderConfigStore {
    val saved: MutableList<ReminderConfig> = mutableListOf()

    override suspend fun load(): ReminderConfig = loaded

    override suspend fun save(config: ReminderConfig) {
        saved += config
    }
}

private class FakeReminderScheduler : ReminderScheduler {
    val scheduled: MutableList<ReminderConfig> = mutableListOf()
    var cancelCount: Int = 0

    override suspend fun schedule(config: ReminderConfig) {
        scheduled += config
    }

    override suspend fun cancelAll() {
        cancelCount += 1
    }
}

private class FakeMotionPreferenceStore(
    initial: Boolean = false,
) : MotionPreferenceStore {
    private val flow = MutableStateFlow(initial)
    val savedValues: MutableList<Boolean> = mutableListOf()

    override fun observeReduceMotionEnabled(): Flow<Boolean> = flow

    override suspend fun loadReduceMotionEnabled(): Boolean = flow.value

    override suspend fun saveReduceMotionEnabled(enabled: Boolean) {
        savedValues += enabled
        flow.value = enabled
    }
}

private class FakeTicketBackupService(
    private val backupResult: Result<TicketBackupSummary> =
        Result.success(
            TicketBackupSummary(0, 0, "tickets_backup_latest.json"),
        ),
    private val restoreResult: Result<TicketBackupSummary> =
        Result.success(
            TicketBackupSummary(0, 0, "tickets_backup_latest.json"),
        ),
    private val integrityResult: Result<TicketBackupIntegritySummary> =
        Result.success(
            TicketBackupIntegritySummary(
                ticketCount = 0,
                gameCount = 0,
                duplicateTicketCount = 0,
                invalidGameCount = 0,
                brokenTicketCount = 0,
                issueCount = 0,
                fileName = "tickets_backup_latest.json",
            ),
        ),
    private val csvExportResult: Result<TicketHistoryCsvSummary> =
        Result.success(
            TicketHistoryCsvSummary(
                ticketCount = 0,
                gameCount = 0,
                roundCount = 0,
                requestedStartRound = null,
                requestedEndRound = null,
                firstRoundNumber = null,
                lastRoundNumber = null,
                matchedDrawCount = 0,
                missingDrawCount = 0,
                missingRoundNumbers = emptyList(),
                generatedGameCount = 0,
                manualGameCount = 0,
                qrGameCount = 0,
                winningGameCount = 0,
                totalExpectedPrizeAmount = 0L,
                fileName = "tickets_history_with_draw_latest.csv",
                filePath = "/tmp/tickets_history_with_draw_latest.csv",
            ),
        ),
) : TicketBackupService {
    var lastExportStartRound: Int? = null
    var lastExportEndRound: Int? = null
    var exportCallCount: Int = 0

    override suspend fun backupCurrentTickets(): Result<TicketBackupSummary> = backupResult

    override suspend fun restoreLatestBackup(): Result<TicketBackupSummary> = restoreResult

    override suspend fun verifyLatestBackupIntegrity(): Result<TicketBackupIntegritySummary> = integrityResult

    override suspend fun exportTicketHistoryCsvForAi(
        startRound: Int?,
        endRound: Int?,
    ): Result<TicketHistoryCsvSummary> {
        exportCallCount += 1
        lastExportStartRound = startRound
        lastExportEndRound = endRound
        return csvExportResult
    }
}
