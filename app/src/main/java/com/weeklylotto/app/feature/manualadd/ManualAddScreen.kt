package com.weeklylotto.app.feature.manualadd

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.MotionButton
import com.weeklylotto.app.ui.component.motionClickable
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors

@Composable
fun ManualAddScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel =
        viewModel<ManualAddViewModel>(
            factory =
                SingleViewModelFactory {
                    ManualAddViewModel(ticketRepository = AppGraph.ticketRepository)
                },
        )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            Toast.makeText(context, "${uiState.savedGameCount}게임 번호를 저장했습니다.", Toast.LENGTH_SHORT).show()
            viewModel.consumeSaved()
            onBack()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LottoColors.Background),
    ) {
        LottoTopAppBar(
            title = "번호 직접 추가",
            rightActionText = "닫기",
            onRightClick = onBack,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("현재 선택 번호 (${uiState.selected.size}/6)", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (index in 0 until 6) {
                    val number = uiState.selected.getOrNull(index)
                    BallChip(number = number, state = if (number == null) BallState.Muted else BallState.Selected)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("추가할 게임 (${uiState.pendingGames.size}/5)", style = MaterialTheme.typography.titleSmall)
                    if (uiState.pendingGames.isEmpty()) {
                        Text("아직 추가된 게임이 없습니다.", color = LottoColors.TextMuted)
                    } else {
                        uiState.pendingGames.forEachIndexed { index, numbers ->
                            val gameLabel = "${('A'.code + index).toChar()} 게임"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("$gameLabel  ${numbers.joinToString(" ")}")
                                Text(
                                    text = "삭제",
                                    color = LottoColors.DangerText,
                                    modifier = Modifier.motionClickable { viewModel.removePendingGame(index) },
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MotionButton(
                            onClick = viewModel::addSelectedGame,
                            enabled = uiState.selected.size == 6 && uiState.pendingGames.size < 5,
                            modifier = Modifier.weight(1f),
                        ) { Text("현재 번호 1게임 추가") }
                        MotionButton(
                            onClick = viewModel::clearPendingGames,
                            enabled = uiState.pendingGames.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                        ) { Text("추가 게임 초기화") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MotionButton(
                            onClick = { viewModel.setRepeatCount(uiState.repeatCount - 1) },
                            enabled = uiState.repeatCount > 1,
                            modifier = Modifier.width(52.dp),
                        ) { Text("-") }
                        Text(
                            text = "같은 번호 반복 ${uiState.repeatCount}게임",
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                            color = LottoColors.TextSecondary,
                        )
                        MotionButton(
                            onClick = { viewModel.setRepeatCount(uiState.repeatCount + 1) },
                            enabled = uiState.repeatCount < 5,
                            modifier = Modifier.width(52.dp),
                        ) { Text("+") }
                    }
                    MotionButton(
                        onClick = viewModel::addSelectedGameRepeated,
                        enabled = uiState.selected.size == 6 && uiState.pendingGames.size < 5,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("같은 번호 반복 추가")
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(9),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items((1..45).toList()) { number ->
                    Box(
                        modifier =
                            Modifier
                                .padding(2.dp)
                                .motionClickable { viewModel.toggleNumber(number) },
                    ) {
                        BallChip(
                            number = number,
                            state =
                                if (number in uiState.selected) {
                                    BallState.Selected
                                } else {
                                    BallState.Muted
                                },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MotionButton(onClick = viewModel::autoFill, modifier = Modifier.weight(1f)) { Text("자동 채우기") }
                MotionButton(onClick = viewModel::clear, modifier = Modifier.weight(1f)) { Text("초기화") }
            }
            uiState.error?.let { message ->
                Text(text = message, color = LottoColors.DangerText)
            }
            MotionButton(
                onClick = viewModel::save,
                enabled = uiState.pendingGames.isNotEmpty() || uiState.selected.size == 6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (uiState.pendingGames.isNotEmpty()) {
                        "${uiState.pendingGames.size}게임 저장"
                    } else {
                        "현재 선택 1게임 저장"
                    },
                )
            }
        }
    }
}
