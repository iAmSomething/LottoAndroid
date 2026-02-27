package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.ReminderConfig
import com.weeklylotto.app.domain.service.MotionPreferenceStore
import com.weeklylotto.app.domain.service.ReminderConfigStore
import com.weeklylotto.app.domain.service.ReminderScheduler
import com.weeklylotto.app.domain.service.TicketBackupService
import com.weeklylotto.app.domain.service.TicketBackupIntegritySummary
import com.weeklylotto.app.domain.service.TicketBackupSummary
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
    private val backupResult: Result<TicketBackupSummary> = Result.success(TicketBackupSummary(0, 0, "tickets_backup_latest.json")),
    private val restoreResult: Result<TicketBackupSummary> = Result.success(TicketBackupSummary(0, 0, "tickets_backup_latest.json")),
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
) : TicketBackupService {
    override suspend fun backupCurrentTickets(): Result<TicketBackupSummary> = backupResult

    override suspend fun restoreLatestBackup(): Result<TicketBackupSummary> = restoreResult

    override suspend fun verifyLatestBackupIntegrity(): Result<TicketBackupIntegritySummary> = integrityResult
}
