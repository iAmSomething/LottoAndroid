package com.weeklylotto.app.feature.generator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.format.toModeLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberGeneratorScreen() {
    val context = LocalContext.current
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

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
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
            modifier = Modifier.weight(1f).padding(horizontal = LottoDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(LottoDimens.CardGap),
        ) {
            items(uiState.games) { game ->
                val isSelectedSlot = game.slot == uiState.selectedSlot
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSlot(game.slot) },
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
                                    modifier = Modifier.clickable { viewModel.toggleNumberLock(game.slot, number) },
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(LottoDimens.ScreenPadding),
            shape = RoundedCornerShape(LottoDimens.CardRadius),
            border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
            colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "${uiState.selectedSlot} 게임 수동 입력",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = {
                            manualInput = it.filter(Char::isDigit).take(2)
                            viewModel.clearManualInputError()
                        },
                        label = { Text("번호(1~45)") },
                        modifier = Modifier.width(130.dp),
                        singleLine = true,
                        isError = uiState.manualInputError != null,
                        supportingText = {
                            uiState.manualInputError?.let { error ->
                                Text(error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Button(onClick = {
                        viewModel.applyManualNumber(uiState.selectedSlot, manualInput)
                        manualInput = ""
                    }) {
                        Text("반영")
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.regenerateExceptLocked() },
                ) {
                    Text("잠금 제외 랜덤 재생성")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.saveCurrentAsWeeklyTicket() },
                ) {
                    Text("이번 주 번호로 저장하기")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
