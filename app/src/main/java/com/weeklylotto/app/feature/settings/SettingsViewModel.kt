package com.weeklylotto.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.domain.model.ReminderConfig
import com.weeklylotto.app.domain.service.MotionPreferenceStore
import com.weeklylotto.app.domain.service.NoOpTicketBackupService
import com.weeklylotto.app.domain.service.ReminderConfigStore
import com.weeklylotto.app.domain.service.ReminderScheduler
import com.weeklylotto.app.domain.service.TicketBackupService
import com.weeklylotto.app.domain.service.TicketHistoryCsvSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

data class SettingsUiState(
    val config: ReminderConfig = ReminderConfig(),
    val reduceMotionEnabled: Boolean = false,
    val message: String? = null,
    val csvShareRequest: CsvShareRequest? = null,
)

data class CsvShareRequest(
    val filePath: String,
    val shareText: String,
    val requestId: Long,
)

@Suppress("TooManyFunctions")
class SettingsViewModel(
    private val reminderConfigStore: ReminderConfigStore,
    private val reminderScheduler: ReminderScheduler,
    private val motionPreferenceStore: MotionPreferenceStore,
    private val ticketBackupService: TicketBackupService = NoOpTicketBackupService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedConfig = reminderConfigStore.load()
            val reduceMotionEnabled = motionPreferenceStore.loadReduceMotionEnabled()
            _uiState.update {
                it.copy(
                    config = savedConfig,
                    reduceMotionEnabled = reduceMotionEnabled,
                )
            }
        }
    }

    fun useDefaultSchedule() {
        _uiState.update { it.copy(config = ReminderConfig()) }
    }

    fun useFridayEveningSchedule() {
        _uiState.update {
            it.copy(
                config =
                    it.config.copy(
                        purchaseReminderDay = DayOfWeek.FRIDAY,
                        purchaseReminderTime = LocalTime.of(18, 30),
                    ),
            )
        }
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(config = it.config.copy(enabled = enabled))
        }
    }

    fun setReduceMotionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(reduceMotionEnabled = enabled) }
        viewModelScope.launch {
            motionPreferenceStore.saveReduceMotionEnabled(enabled)
        }
    }

    fun saveSchedule() {
        viewModelScope.launch {
            val config = _uiState.value.config
            reminderConfigStore.save(config)
            reminderScheduler.schedule(config)
            _uiState.update { it.copy(message = "알림 설정을 저장했습니다.") }
        }
    }

    fun backupTickets() {
        viewModelScope.launch {
            ticketBackupService
                .backupCurrentTickets()
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            message = "백업 파일 생성 완료 (${summary.ticketCount}건, ${summary.gameCount}게임)",
                        )
                    }
                }.onFailure {
                    _uiState.update { state ->
                        state.copy(message = "백업 파일 생성에 실패했습니다.")
                    }
                }
        }
    }

    fun restoreTicketsFromBackup() {
        viewModelScope.launch {
            ticketBackupService
                .restoreLatestBackup()
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            message = "백업 복원 완료 (${summary.ticketCount}건, ${summary.gameCount}게임)",
                        )
                    }
                }.onFailure {
                    _uiState.update { state ->
                        state.copy(message = "백업 복원에 실패했습니다.")
                    }
                }
        }
    }

    fun verifyBackupIntegrity() {
        viewModelScope.launch {
            ticketBackupService
                .verifyLatestBackupIntegrity()
                .onSuccess { summary ->
                    val message =
                        if (summary.issueCount == 0) {
                            "무결성 점검 완료 (문제 없음: ${summary.ticketCount}건)"
                        } else {
                            "무결성 점검 완료 (문제 ${summary.issueCount}건: 중복 ${summary.duplicateTicketCount}, 게임오류 ${summary.invalidGameCount}, 레코드오류 ${summary.brokenTicketCount})"
                        }
                    _uiState.update { it.copy(message = message) }
                }.onFailure {
                    _uiState.update { state ->
                        state.copy(message = "무결성 점검에 실패했습니다.")
                    }
                }
        }
    }

    fun exportTicketHistoryCsvForAi() {
        viewModelScope.launch {
            ticketBackupService
                .exportTicketHistoryCsvForAi()
                .onSuccess { summary ->
                    val drawCoverageMessage =
                        if (summary.missingDrawCount == 0) {
                            "당첨번호 포함 ${summary.matchedDrawCount}회차"
                        } else {
                            "당첨번호 누락 ${summary.missingDrawCount}회차"
                        }
                    val winningSummaryMessage =
                        "당첨게임 ${summary.winningGameCount}개, 예상당첨금 ${summary.totalExpectedPrizeAmount}원"
                    _uiState.update {
                        it.copy(
                            message =
                                "CSV 생성 완료 (${summary.roundCount}회차, ${summary.ticketCount}건, ${summary.gameCount}게임, $drawCoverageMessage, $winningSummaryMessage)",
                            csvShareRequest =
                                CsvShareRequest(
                                    filePath = summary.filePath,
                                    shareText = buildAiShareText(summary),
                                    requestId = System.currentTimeMillis(),
                                ),
                        )
                    }
                }.onFailure {
                    _uiState.update { state ->
                        state.copy(
                            message = "CSV 생성에 실패했습니다.",
                            csvShareRequest = null,
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearCsvShareRequest() {
        _uiState.update { it.copy(csvShareRequest = null) }
    }

    private fun buildAiShareText(summary: TicketHistoryCsvSummary): String =
        buildString {
            appendLine("로또 주차별 구매/당첨 CSV 분석 요청")
            appendLine("- 회차 수: ${summary.roundCount}")
            appendLine("- 티켓 수: ${summary.ticketCount}")
            appendLine("- 게임 수: ${summary.gameCount}")
            appendLine("- 당첨번호 매칭 회차: ${summary.matchedDrawCount}")
            appendLine("- 당첨 게임 수: ${summary.winningGameCount}")
            appendLine("- 예상 당첨금 합계: ${summary.totalExpectedPrizeAmount}원")
            if (summary.missingDrawCount > 0) {
                appendLine("- 경고: 당첨번호가 없는 회차 ${summary.missingDrawCount}개 포함")
            }
            appendLine()
            appendLine("요청:")
            appendLine("1) 최근/누적 기준 번호 패턴과 중복 조합 리스크를 요약해줘.")
            appendLine("2) 출처별(자동/수동/QR) 성과 차이를 분석해줘.")
            appendLine("3) 다음 주차용 번호 전략 3가지를 제안해줘.")
        }
}
