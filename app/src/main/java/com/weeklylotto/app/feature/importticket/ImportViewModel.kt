package com.weeklylotto.app.feature.importticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.data.RoundEstimator
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

data class ImportUiState(
    val input: String = "",
    val parsedNumbers: List<Int> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
)

class ImportViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onInputChanged(value: String) {
        _uiState.update { it.copy(input = value, saved = false) }
    }

    fun parse() {
        val tokens = Regex("\\d+").findAll(uiState.value.input).map { it.value.toInt() }.toList()
        if (tokens.size != 6 || tokens.any { it !in 1..45 } || tokens.distinct().size != 6) {
            _uiState.update {
                it.copy(
                    parsedNumbers = emptyList(),
                    error = "1~45 범위의 중복 없는 6개 숫자를 입력하세요.",
                )
            }
            return
        }
        _uiState.update { it.copy(parsedNumbers = tokens.sorted(), error = null) }
    }

    fun save() {
        val numbers = uiState.value.parsedNumbers
        if (numbers.size != 6) return
        viewModelScope.launch {
            val today = LocalDate.now()
            val drawDate = RoundEstimator.nextDrawDate(today)
            ticketRepository.save(
                TicketBundle(
                    round = Round(RoundEstimator.currentSalesRound(today), drawDate),
                    source = TicketSource.MANUAL,
                    games =
                        listOf(
                            LottoGame(
                                slot = GameSlot.A,
                                numbers = numbers.map(::LottoNumber),
                                lockedNumbers = numbers.map(::LottoNumber).toSet(),
                                mode = GameMode.MANUAL,
                            ),
                        ),
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }
}
