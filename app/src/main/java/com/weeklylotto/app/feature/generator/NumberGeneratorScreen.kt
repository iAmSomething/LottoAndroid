package com.weeklylotto.app.feature.generator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.service.AnalyticsActionValue
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.format.toModeLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Suppress("CyclomaticComplexMethod")
fun NumberGeneratorScreen() {
    val context = LocalContext.current
    val analyticsLogger = AppGraph.analyticsLogger
    val viewModel =
        viewModel<NumberGeneratorViewModel>(
            factory =
                SingleViewModelFactory {
                    NumberGeneratorViewModel(
                        numberGenerator = AppGraph.numberGenerator,
                        ticketRepository = AppGraph.ticketRepository,
                    )
                },
        )
    val uiState by viewModel.uiState.collectAsState()

    var manualInput by remember { mutableStateOf("") }
    var selectedManualNumber by remember(uiState.selectedSlot) { mutableStateOf<Int?>(null) }
    var selectedReplaceTarget by remember(uiState.selectedSlot) { mutableStateOf<Int?>(null) }
    val selectedGame = uiState.games.firstOrNull { it.slot == uiState.selectedSlot }
    val selectedNumbers = selectedGame?.numbers?.toSet().orEmpty()
    val selectedLockedNumbers = selectedGame?.lockedNumbers.orEmpty()
    val quickCandidates =
        remember(selectedGame) {
            (1..45).filter { LottoNumber(it) !in selectedNumbers }.take(8)
        }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(selectedGame, selectedReplaceTarget) {
        val hasValidTarget =
            selectedReplaceTarget != null &&
                selectedGame != null &&
                selectedGame.numbers.any { it.value == selectedReplaceTarget } &&
                selectedGame.lockedNumbers.none { it.value == selectedReplaceTarget }
        if (selectedReplaceTarget != null && !hasValidTarget) {
            selectedReplaceTarget = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "번호 생성",
            rightActionText = "전체 초기화",
            onRightClick = viewModel::resetAllGames,
        )

        Card(
            modifier = Modifier.padding(horizontal = LottoDimens.ScreenPadding, vertical = LottoDimens.CardGap),
            shape = RoundedCornerShape(LottoDimens.CardRadius),
            border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
            colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
        ) {
            Column(modifier = Modifier.padding(LottoDimens.ScreenPadding)) {
                Text("공을 탭해 선택 · 길게 눌러 고정", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                Text("고정된 번호는 재생성 시 유지됩니다.", color = LottoColors.TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }

        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = LottoDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(LottoDimens.CardGap),
        ) {
            items(uiState.games) { game ->
                val isSelectedSlot = game.slot == uiState.selectedSlot
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectSlot(game.slot)
                                selectedReplaceTarget = null
                            },
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border =
                        androidx.compose.foundation.BorderStroke(
                            width = if (isSelectedSlot) 2.dp else 1.dp,
                            color =
                                if (isSelectedSlot) {
                                    LottoColors.Primary.copy(alpha = 0.35f)
                                } else {
                                    LottoColors.Border
                                },
                        ),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(LottoDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "${game.slot} 게임",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                game.mode.toModeLabel(),
                                color = LottoColors.Primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(LottoDimens.BallGap)) {
                            game.numbers.forEach { number ->
                                BallChip(
                                    number = number.value,
                                    state = if (number in game.lockedNumbers) BallState.Locked else BallState.Selected,
                                    size = LottoDimens.BallSizeLarge,
                                    modifier =
                                        Modifier.clickable {
                                            analyticsLogger.log(
                                                event = AnalyticsEvent.INTERACTION_BALL_LOCK_TOGGLE,
                                                params =
                                                    mapOf(
                                                        AnalyticsParamKey.SCREEN to "generator",
                                                        AnalyticsParamKey.COMPONENT to "number_ball",
                                                        AnalyticsParamKey.ACTION to if (number in game.lockedNumbers) AnalyticsActionValue.UNLOCK else AnalyticsActionValue.LOCK,
                                                        "slot" to game.slot.name,
                                                        "number" to number.value.toString(),
                                                    ),
                                            )
                                            viewModel.toggleNumberLock(game.slot, number)
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(LottoDimens.ScreenPadding),
            shape = RoundedCornerShape(LottoDimens.CardRadius),
            border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
            colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "${uiState.selectedSlot} 게임 수동 입력 편집",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    "게임 선택 -> 번호 선택 -> 반영 순서로 진행하세요.",
                    color = LottoColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                selectedGame?.let { game ->
                    Text(
                        "현재 번호",
                        color = LottoColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        game.numbers.forEach { number ->
                            val state =
                                if (number in game.lockedNumbers) {
                                    BallState.Locked
                                } else if (selectedReplaceTarget == number.value) {
                                    BallState.Hit
                                } else {
                                    BallState.Selected
                                }
                            BallChip(
                                number = number.value,
                                state = state,
                                size = 30.dp,
                                modifier =
                                    if (number in game.lockedNumbers) {
                                        Modifier
                                    } else {
                                        Modifier.clickable {
                                            selectedReplaceTarget =
                                                if (selectedReplaceTarget == number.value) {
                                                    null
                                                } else {
                                                    number.value
                                                }
                                        }
                                    },
                            )
                        }
                    }
                    Text(
                        "현재 번호를 탭하면 교체 대상을 지정할 수 있습니다.",
                        color = LottoColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Text(
                    "빠른 후보",
                    color = LottoColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    quickCandidates.forEach { number ->
                        FilterChip(
                            selected = selectedManualNumber == number,
                            onClick = {
                                selectedManualNumber = number
                                manualInput = number.toString()
                                viewModel.clearManualInputError()
                            },
                            label = { Text(number.toString().padStart(2, '0')) },
                        )
                    }
                }

                Text(
                    "번호 팔레트",
                    color = LottoColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (number in 1..45) {
                        val lottoNumber = LottoNumber(number)
                        val state =
                            when {
                                selectedManualNumber == number -> BallState.Hit
                                lottoNumber in selectedLockedNumbers -> BallState.Locked
                                lottoNumber in selectedNumbers -> BallState.Selected
                                else -> BallState.Muted
                            }
                        BallChip(
                            number = number,
                            state = state,
                            size = 30.dp,
                            modifier =
                                Modifier.clickable {
                                    selectedManualNumber = number
                                    manualInput = number.toString()
                                    viewModel.clearManualInputError()
                                },
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = {
                            manualInput = it.filter(Char::isDigit).take(2)
                            selectedManualNumber = manualInput.toIntOrNull()?.takeIf { value -> value in 1..45 }
                            viewModel.clearManualInputError()
                        },
                        label = { Text("번호(1~45)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = uiState.manualInputError != null,
                        supportingText = {
                            uiState.manualInputError?.let { error ->
                                Text(error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Button(
                        onClick = {
                            analyticsLogger.log(
                                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                params =
                                    mapOf(
                                        AnalyticsParamKey.SCREEN to "generator",
                                        AnalyticsParamKey.COMPONENT to "manual_apply",
                                        AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                    ),
                            )
                            val raw = selectedManualNumber?.toString() ?: manualInput
                            viewModel.applyManualNumber(
                                slot = uiState.selectedSlot,
                                rawInput = raw,
                                replaceTargetNumber = selectedReplaceTarget,
                            )
                            selectedReplaceTarget = null
                        },
                        enabled = selectedManualNumber != null || manualInput.isNotBlank(),
                    ) {
                        Text("선택 반영")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            selectedManualNumber = null
                            manualInput = ""
                            selectedReplaceTarget = null
                            viewModel.clearManualInputError()
                        },
                    ) {
                        Text("입력 초기화")
                    }
                    Text(
                        text =
                            "선택 번호: ${selectedManualNumber?.toString()?.padStart(2, '0') ?: "-"}  ·  교체 대상: ${
                                selectedReplaceTarget?.toString()?.padStart(2, '0') ?: "자동"
                            }",
                        color = LottoColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "검은 테두리 번호는 잠금 상태입니다.",
                    color = LottoColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        analyticsLogger.log(
                            event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                            params =
                                mapOf(
                                    AnalyticsParamKey.SCREEN to "generator",
                                    AnalyticsParamKey.COMPONENT to "regenerate_except_locked",
                                    AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                ),
                        )
                        viewModel.regenerateExceptLocked()
                    },
                ) {
                    Text("잠금 제외 랜덤 재생성")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        analyticsLogger.log(
                            event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                            params =
                                mapOf(
                                    AnalyticsParamKey.SCREEN to "generator",
                                    AnalyticsParamKey.COMPONENT to "regenerate_save_weekly_ticket",
                                    AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                ),
                        )
                        viewModel.regenerateAndSaveAsWeeklyTicket()
                    },
                ) {
                    Text("랜덤 생성 후 바로 저장")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        analyticsLogger.log(
                            event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                            params =
                                mapOf(
                                    AnalyticsParamKey.SCREEN to "generator",
                                    AnalyticsParamKey.COMPONENT to "save_weekly_ticket",
                                    AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                ),
                        )
                        viewModel.saveCurrentAsWeeklyTicket()
                    },
                ) {
                    Text("이번 주 번호로 저장하기")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
