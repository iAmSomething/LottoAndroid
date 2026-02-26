package com.weeklylotto.app.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.ui.component.BadgeTone
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.StatusBadge
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("CyclomaticComplexMethod")
fun ResultScreen() {
    val viewModel =
        viewModel<ResultViewModel>(
            factory =
                SingleViewModelFactory {
                    ResultViewModel(
                        drawRepository = AppGraph.drawRepository,
                        ticketRepository = AppGraph.ticketRepository,
                        evaluator = AppGraph.resultEvaluator,
                    )
                },
        )

    val uiState by viewModel.uiState.collectAsState()
    var isRoundSheetOpen by remember { mutableStateOf(false) }
    var pendingRound by remember(uiState.selectedRound, isRoundSheetOpen) { mutableStateOf(uiState.selectedRound) }
    val openRoundSheet = {
        uiState.drawResult?.let { currentDraw ->
            pendingRound = uiState.selectedRound ?: currentDraw.round.number
            isRoundSheetOpen = true
        }
    }

    val drawResult = uiState.drawResult
    if (isRoundSheetOpen && drawResult != null) {
        val rounds = uiState.availableRounds.ifEmpty { listOf(drawResult.round.number) }
        ModalBottomSheet(
            onDismissRequest = { isRoundSheetOpen = false },
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("회차 변경", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                rounds.forEach { round ->
                    val selected = round == pendingRound
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (selected) LottoColors.Border.copy(alpha = 0.35f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable {
                                    pendingRound = round
                                }
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { pendingRound = round },
                        )
                        Text(text = "${round}회")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = { isRoundSheetOpen = false },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            pendingRound?.let(viewModel::selectRound)
                            isRoundSheetOpen = false
                        },
                        enabled = pendingRound != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("적용")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "당첨 결과",
            rightActionText = uiState.drawResult?.round?.number?.let { "${it}회" } ?: "새로고침",
            onRightClick = {
                if (uiState.drawResult != null) {
                    openRoundSheet()
                } else {
                    viewModel.refresh()
                }
            },
        )

        if (uiState.loading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                if (uiState.hasRetried) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "재시도 중 (${uiState.retryAttempt}/${uiState.maxRetryAttempt})",
                        color = LottoColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            return
        }

        if (uiState.error != null) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = uiState.error?.title ?: "결과를 불러오지 못했습니다",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = uiState.error?.message ?: "잠시 후 다시 시도해 주세요.",
                        color = Color(0xFF424242),
                    )
                    if (uiState.hasRetried) {
                        Text(
                            text = "총 ${uiState.maxRetryAttempt}회 시도 후 실패했습니다.",
                            color = LottoColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    uiState.lastErrorAt?.let { failedAt ->
                        Text(
                            text = "최근 실패 시각 ${failedAt.format(errorTimeFormatter)}",
                            color = LottoColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Button(onClick = viewModel::refresh) {
                        Text("다시 시도")
                    }
                    if (uiState.selectedRound != null) {
                        TextButton(onClick = viewModel::loadLatestFromError) {
                            Text("최신 회차 조회")
                        }
                    }
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LottoDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(LottoDimens.SectionGap),
        ) {
            item {
                val draw = uiState.drawResult
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(LottoDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "제 ${draw?.round?.number}회 당첨 결과",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("당첨 번호", color = LottoColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                            draw?.mainNumbers?.forEach {
                                BallChip(
                                    number = it.value,
                                    state = BallState.Selected,
                                    size = LottoDimens.BallSizeLarge,
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("보너스", color = LottoColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                            draw?.bonus?.let {
                                BallChip(
                                    number = it.value,
                                    state = BallState.Bonus,
                                    size = LottoDimens.BallSizeLarge,
                                )
                            }
                        }
                        Text(
                            text = "${draw?.drawDate} 추첨",
                            color = LottoColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "내 구매 번호 (${uiState.evaluatedGames.size}게임)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "회차 변경",
                        color = LottoColors.Primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable { openRoundSheet() },
                    )
                }
            }

            if (uiState.evaluatedGames.isEmpty()) {
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("해당 회차에 저장된 번호가 없습니다.")
                        }
                    }
                }
            }

            items(uiState.evaluatedGames) { evaluated ->
                val highlight = evaluated.result.rank != DrawRank.NONE
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(LottoDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${evaluated.game.slot.name} 게임",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                            )
                            StatusBadge(
                                label = evaluated.result.rank.label,
                                tone = if (highlight) BadgeTone.Accent else BadgeTone.Neutral,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            evaluated.game.numbers.forEach { number ->
                                val state =
                                    when {
                                        number in evaluated.result.highlightedNumbers -> BallState.Hit
                                        evaluated.result.bonusMatched &&
                                            uiState.drawResult?.bonus == number ->
                                            BallState.Bonus
                                        else -> BallState.Muted
                                    }
                                BallChip(number = number.value, state = state, size = LottoDimens.BallSizeLarge)
                            }
                        }
                        Text(
                            text =
                                "적중 ${evaluated.result.matchedMainCount}개" +
                                    if (evaluated.result.bonusMatched) " + 보너스" else "",
                            color = LottoColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

private val errorTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
