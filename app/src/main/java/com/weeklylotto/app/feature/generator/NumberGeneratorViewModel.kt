package com.weeklylotto.app.feature.generator

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
import com.weeklylotto.app.domain.service.NumberGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

data class NumberGeneratorUiState(
    val games: List<LottoGame> = emptyList(),
    val selectedSlot: GameSlot = GameSlot.A,
    val toastMessage: String? = null,
    val manualInputError: String? = null,
)

@Suppress("TooManyFunctions")
class NumberGeneratorViewModel(
    private val numberGenerator: NumberGenerator,
    private val ticketRepository: TicketRepository,
    private val random: Random = Random.Default,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NumberGeneratorUiState(games = numberGenerator.generateInitialGames()))
    val uiState: StateFlow<NumberGeneratorUiState> = _uiState.asStateFlow()
    private var historicalBundles: List<TicketBundle> = emptyList()

    init {
        viewModelScope.launch {
            ticketRepository.observeAllTickets().collect { bundles ->
                historicalBundles = bundles
            }
        }
    }

    fun selectSlot(slot: GameSlot) {
        _uiState.update { it.copy(selectedSlot = slot, manualInputError = null) }
    }

    fun toggleNumberLock(
        slot: GameSlot,
        number: LottoNumber,
    ) {
        _uiState.update { state ->
            val updatedGames =
                state.games.map { game ->
                    if (game.slot != slot) return@map game

                    val updatedLocked =
                        game.lockedNumbers.toMutableSet().apply {
                            if (contains(number)) remove(number) else add(number)
                        }

                    val updatedMode =
                        when {
                            updatedLocked.isEmpty() -> GameMode.AUTO
                            updatedLocked.size == 6 -> GameMode.MANUAL
                            else -> GameMode.SEMI_AUTO
                        }

                    game.copy(lockedNumbers = updatedLocked, mode = updatedMode)
                }
            state.copy(games = updatedGames)
        }
    }

    fun applyManualNumber(
        slot: GameSlot,
        rawInput: String,
        replaceTargetNumber: Int? = null,
    ) {
        val validation = validateManualInput(rawInput)
        if (validation is ManualInputValidation.Error) {
            _uiState.update { it.copy(manualInputError = validation.message, toastMessage = null) }
            return
        }

        val input = (validation as ManualInputValidation.Valid).number
        _uiState.update { state ->
            val replaceTarget =
                replaceTargetNumber
                    ?.takeIf { it in 1..45 }
                    ?.let(::LottoNumber)
            val result = applyManualInputToGames(state.games, slot, input, replaceTarget)
            state.copy(games = result.games, toastMessage = result.message, manualInputError = result.manualInputError)
        }
    }

    fun applyManualNumber(
        slot: GameSlot,
        number: Int,
        replaceTargetNumber: Int? = null,
    ) {
        applyManualNumber(slot = slot, rawInput = number.toString(), replaceTargetNumber = replaceTargetNumber)
    }

    fun regenerateExceptLocked() {
        _uiState.update { state ->
            state.copy(
                games = numberGenerator.regenerateExceptLocked(state.games),
                toastMessage = "잠금 번호를 제외하고 재생성했습니다.",
            )
        }
    }

    fun resetAllGames() {
        _uiState.update {
            it.copy(
                games = numberGenerator.generateInitialGames(),
                manualInputError = null,
                toastMessage = "전체 번호를 초기화했습니다.",
            )
        }
    }

    fun regenerateAndSaveAsWeeklyTicket() {
        _uiState.update { state ->
            state.copy(
                games = numberGenerator.regenerateExceptLocked(state.games),
                manualInputError = null,
                toastMessage = null,
            )
        }
        saveCurrentAsWeeklyTicket(successMessage = "잠금 번호 기준으로 새 번호를 생성해 저장했습니다.")
    }

    fun generatePreferredPatternGames() {
        _uiState.update { state ->
            val result =
                generatePreferredPatternGames(
                    currentGames = state.games,
                    history = historicalBundles,
                    random = random,
                )
            state.copy(
                games = result.games,
                manualInputError = null,
                toastMessage = result.message,
            )
        }
    }

    fun saveCurrentAsWeeklyTicket(
        successMessage: String = "이번 주 번호를 저장했습니다. 동일 회차 자동번호는 최신 저장본으로 갱신됩니다.",
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val drawDate = RoundEstimator.nextDrawDate(today)
            val round =
                Round(
                    number = RoundEstimator.currentSalesRound(today),
                    drawDate = drawDate,
                )
            val bundle =
                TicketBundle(
                    round = round,
                    games = uiState.value.games,
                    source = TicketSource.GENERATED,
                )
            ticketRepository.save(bundle)
            _uiState.update {
                it.copy(
                    toastMessage = successMessage,
                    manualInputError = null,
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun clearManualInputError() {
        _uiState.update { it.copy(manualInputError = null) }
    }
}

private sealed interface ManualInputValidation {
    data class Valid(val number: LottoNumber) : ManualInputValidation

    data class Error(val message: String) : ManualInputValidation
}

private data class ManualApplyResult(
    val games: List<LottoGame>,
    val message: String,
    val manualInputError: String? = null,
)

private data class PreferredPatternResult(
    val games: List<LottoGame>,
    val message: String,
)

private enum class ManualApplyAction {
    REPLACED,
    LOCKED_EXISTING,
    ALREADY_LOCKED,
    ALL_LOCKED,
    INVALID_TARGET,
}

private fun generatePreferredPatternGames(
    currentGames: List<LottoGame>,
    history: List<TicketBundle>,
    random: Random,
): PreferredPatternResult {
    val result =
        if (currentGames.isEmpty()) {
            PreferredPatternResult(
                games = currentGames,
                message = "추천 생성 대상이 없습니다.",
            )
        } else {
            val frequencies =
                history
                    .asSequence()
                    .flatMap { bundle -> bundle.games.asSequence() }
                    .flatMap { game -> game.numbers.asSequence() }
                    .groupingBy { number -> number.value }
                    .eachCount()

            if (frequencies.isEmpty()) {
                PreferredPatternResult(
                    games = currentGames,
                    message = "추천 데이터가 없어 현재 번호를 유지합니다.",
                )
            } else {
                val topNumbers =
                    frequencies
                        .entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { it.key }

                val recommended =
                    currentGames.map { game ->
                        game.copy(
                            numbers = pickWeightedUniqueNumbers(frequencies, random),
                            lockedNumbers = emptySet(),
                            mode = GameMode.AUTO,
                        )
                    }
                val topSummary = topNumbers.joinToString(", ") { number -> number.toString().padStart(2, '0') }
                PreferredPatternResult(
                    games = recommended,
                    message = "선호 패턴 추천 번호를 생성했습니다. (상위 빈도: $topSummary)",
                )
            }
        }

    return result
}

private fun pickWeightedUniqueNumbers(
    frequencies: Map<Int, Int>,
    random: Random,
): List<LottoNumber> {
    val available = (1..45).toMutableSet()
    val chosen = mutableListOf<LottoNumber>()

    repeat(6) {
        var totalWeight = 0
        val weights =
            available.associateWith { number ->
                // Keep all numbers selectable while biasing towards frequent picks.
                val weight = (frequencies[number] ?: 0) + 1
                totalWeight += weight
                weight
            }

        var threshold = random.nextInt(totalWeight)
        var selected = available.first()
        for (number in available.sorted()) {
            threshold -= weights.getValue(number)
            if (threshold < 0) {
                selected = number
                break
            }
        }

        available.remove(selected)
        chosen += LottoNumber(selected)
    }

    return chosen.sortedBy { it.value }
}

private fun validateManualInput(rawInput: String): ManualInputValidation =
    when {
        rawInput.isBlank() -> ManualInputValidation.Error("번호를 입력해주세요.")
        rawInput.toIntOrNull() == null -> ManualInputValidation.Error("숫자만 입력할 수 있습니다.")
        rawInput.toInt() !in 1..45 -> ManualInputValidation.Error("1~45 범위만 입력할 수 있습니다.")
        else -> ManualInputValidation.Valid(LottoNumber(rawInput.toInt()))
    }

private fun applyManualInputToGames(
    games: List<LottoGame>,
    slot: GameSlot,
    input: LottoNumber,
    replaceTarget: LottoNumber?,
): ManualApplyResult {
    val targetIndex = games.indexOfFirst { it.slot == slot }
    if (targetIndex < 0) {
        return ManualApplyResult(
            games = games,
            message = "선택한 게임을 찾을 수 없습니다.",
            manualInputError = "선택한 게임을 찾을 수 없습니다.",
        )
    }

    val (updatedGame, action) = applyManualInputToGame(games[targetIndex], input, replaceTarget)
    val updatedGames = games.toMutableList().apply { set(targetIndex, updatedGame) }

    return when (action) {
        ManualApplyAction.REPLACED ->
            ManualApplyResult(
                games = updatedGames,
                message = "${input.value} 번을 ${slot.name} 게임에 반영했습니다.",
            )

        ManualApplyAction.LOCKED_EXISTING ->
            ManualApplyResult(
                games = updatedGames,
                message = "${input.value} 번이 이미 포함되어 고정 처리했습니다.",
            )

        ManualApplyAction.ALREADY_LOCKED ->
            ManualApplyResult(
                games = updatedGames,
                message = "이미 고정된 번호입니다.",
                manualInputError = "이미 고정된 번호입니다.",
            )

        ManualApplyAction.ALL_LOCKED ->
            ManualApplyResult(
                games = updatedGames,
                message = "해당 게임은 6개 번호가 모두 고정되어 교체할 수 없습니다.",
                manualInputError = "해당 게임은 6개 번호가 모두 고정되어 교체할 수 없습니다.",
            )

        ManualApplyAction.INVALID_TARGET ->
            ManualApplyResult(
                games = updatedGames,
                message = "교체 대상을 다시 선택해주세요.",
                manualInputError = "잠금되지 않은 번호를 교체 대상으로 선택해주세요.",
            )
    }
}

private fun applyManualInputToGame(
    game: LottoGame,
    input: LottoNumber,
    replaceTarget: LottoNumber?,
): Pair<LottoGame, ManualApplyAction> =
    when {
        input in game.lockedNumbers -> game to ManualApplyAction.ALREADY_LOCKED
        input in game.numbers -> {
            val updatedLocked = game.lockedNumbers + input
            game.copy(
                lockedNumbers = updatedLocked,
                mode = resolveMode(updatedLocked.size),
            ) to ManualApplyAction.LOCKED_EXISTING
        }
        game.lockedNumbers.size == game.numbers.size -> game to ManualApplyAction.ALL_LOCKED
        else -> {
            val target =
                when {
                    replaceTarget == null -> {
                        game.numbers.firstOrNull { it !in game.lockedNumbers }
                            ?: game.numbers.last()
                    }
                    replaceTarget !in game.numbers || replaceTarget in game.lockedNumbers -> {
                        return game to ManualApplyAction.INVALID_TARGET
                    }
                    else -> replaceTarget
                }

            val replacedNumbers =
                game.numbers.toMutableList().apply {
                    remove(target)
                    add(input)
                }.distinct().sortedBy { it.value }

            val filledNumbers =
                if (replacedNumbers.size < 6) {
                    val additional =
                        (1..45)
                            .map(::LottoNumber)
                            .filterNot { it in replacedNumbers }
                            .shuffled(Random.Default)
                            .take(6 - replacedNumbers.size)
                    (replacedNumbers + additional).sortedBy { it.value }
                } else {
                    replacedNumbers
                }

            val locked = game.lockedNumbers + input
            game.copy(
                numbers = filledNumbers,
                lockedNumbers = locked,
                mode = resolveMode(locked.size),
            ) to ManualApplyAction.REPLACED
        }
    }

private fun resolveMode(lockedCount: Int): GameMode =
    when {
        lockedCount <= 0 -> GameMode.AUTO
        lockedCount >= 6 -> GameMode.MANUAL
        else -> GameMode.SEMI_AUTO
    }
