package com.weeklylotto.app.feature.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.data.qr.QrTicketParser
import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PendingScanTicket(
    val round: Int,
    val games: List<LottoGame>,
)

data class QrScanUiState(
    val latestMessage: String? = null,
    val continuousScanEnabled: Boolean = true,
    val savedTicketCount: Int = 0,
    val lastSavedRound: Int? = null,
    val consecutiveFailureCount: Int = 0,
    val failureGuideMessage: String? = null,
    val pendingScan: PendingScanTicket? = null,
)

class QrScanViewModel(
    private val parser: QrTicketParser,
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QrScanUiState())
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()

    fun setContinuousScan(enabled: Boolean) {
        _uiState.update { it.copy(continuousScanEnabled = enabled) }
    }

    fun resetSessionCount() {
        _uiState.update {
            it.copy(
                savedTicketCount = 0,
                lastSavedRound = null,
                consecutiveFailureCount = 0,
                failureGuideMessage = null,
                pendingScan = null,
            )
        }
    }

    fun parseForConfirm(rawUrl: String) {
        if (rawUrl.isBlank()) {
            _uiState.update { it.copy(latestMessage = "QR URL을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            when (val parsed = parser.parse(rawUrl)) {
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            latestMessage = "파싱 실패: ${parsed.error}",
                            consecutiveFailureCount = it.consecutiveFailureCount + 1,
                            failureGuideMessage = toFailureGuide(parsed.error),
                            pendingScan = null,
                        )
                    }
                }

                is AppResult.Success -> {
                    val ticket = parsed.value
                    if (ticket.games.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                latestMessage = "QR에 게임 정보가 없습니다.",
                                consecutiveFailureCount = it.consecutiveFailureCount + 1,
                                failureGuideMessage = "티켓 QR이 완전히 보이도록 촬영해 다시 시도하세요.",
                                pendingScan = null,
                            )
                        }
                        return@launch
                    }

                    val games = mutableListOf<LottoGame>()
                    for ((index, numbers) in ticket.games.take(5).withIndex()) {
                        val normalizedNumbers = numbers.distinct().sortedBy(LottoNumber::value)
                        if (normalizedNumbers.size != 6) {
                            _uiState.update {
                                it.copy(
                                    latestMessage = "QR 번호 형식이 올바르지 않습니다. (각 게임은 6개 번호)",
                                    consecutiveFailureCount = it.consecutiveFailureCount + 1,
                                    failureGuideMessage = "QR 일부가 가려졌을 수 있습니다. 용지 한 장만 가까이 촬영해 다시 시도하세요.",
                                    pendingScan = null,
                                )
                            }
                            return@launch
                        }

                        games +=
                            LottoGame(
                                slot = GameSlot.entries[index],
                                numbers = normalizedNumbers,
                                lockedNumbers = normalizedNumbers.toSet(),
                                mode = GameMode.MANUAL,
                            )
                    }

                    _uiState.update {
                        it.copy(
                            latestMessage = "QR 인식 완료: ${ticket.round}회차 ${games.size}게임",
                            consecutiveFailureCount = 0,
                            failureGuideMessage = null,
                            pendingScan = PendingScanTicket(round = ticket.round, games = games),
                        )
                    }
                }
            }
        }
    }

    fun confirmPendingSave() {
        val pending = uiState.value.pendingScan ?: return
        viewModelScope.launch {
            val drawDate = nextSaturday(LocalDate.now())
            ticketRepository.save(
                TicketBundle(
                    round = Round(pending.round, drawDate),
                    games = pending.games,
                    source = TicketSource.QR_SCAN,
                ),
            )
            _uiState.update {
                it.copy(
                    latestMessage = "QR 구매번호를 ${pending.round}회차로 저장했습니다.",
                    savedTicketCount = it.savedTicketCount + 1,
                    lastSavedRound = pending.round,
                    consecutiveFailureCount = 0,
                    failureGuideMessage = null,
                    pendingScan = null,
                )
            }
        }
    }

    fun cancelPendingSave() {
        _uiState.update { it.copy(pendingScan = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(latestMessage = null) }
    }

    fun clearFailureGuide() {
        _uiState.update { it.copy(consecutiveFailureCount = 0, failureGuideMessage = null) }
    }

    private fun nextSaturday(from: LocalDate): LocalDate {
        var candidate = from
        while (candidate.dayOfWeek != java.time.DayOfWeek.SATURDAY) {
            candidate = candidate.plusDays(1)
        }
        return candidate
    }

    private fun toFailureGuide(error: AppError): String =
        when (error) {
            is AppError.ParseError ->
                when {
                    error.message.contains("지원하지 않는", ignoreCase = true) ->
                        "동행복권 QR이 맞는지 확인하고, 카메라를 티켓 우측 상단 QR에 더 가깝게 맞춰주세요."
                    error.message.contains("payload", ignoreCase = true) ->
                        "QR 정보가 일부만 인식되었습니다. 밝은 곳에서 용지 한 장만 프레임에 넣고 다시 스캔하세요."
                    else ->
                        "QR 인식이 불안정합니다. 손떨림을 줄이고 15~20cm 거리에서 다시 시도하세요."
                }

            else -> "일시적인 오류입니다. 잠시 후 다시 시도하거나 수동 입력을 이용하세요."
        }
}
