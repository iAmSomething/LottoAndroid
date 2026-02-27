package com.weeklylotto.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.LottoBall
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.format.toSourceChipLabel
import com.weeklylotto.app.ui.format.toWonLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens
import kotlin.math.abs

@Composable
fun StatsScreen() {
    val viewModel =
        viewModel<StatsViewModel>(
            factory =
                SingleViewModelFactory {
                    StatsViewModel(
                        ticketRepository = AppGraph.ticketRepository,
                        drawRepository = AppGraph.drawRepository,
                        resultEvaluator = AppGraph.resultEvaluator,
                    )
                },
        )

    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(title = "통계")

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(LottoDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(LottoDimens.CardGap),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("조회 기간", style = MaterialTheme.typography.bodyMedium, color = LottoColors.TextMuted)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StatsPeriod.entries.forEach { period ->
                                val selected = uiState.selectedPeriod == period
                                Button(onClick = { viewModel.setPeriod(period) }) {
                                    Text(if (selected) "✓ ${period.label}" else period.label)
                                }
                            }
                        }
                        Text("직접 회차 범위", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = uiState.customStartRound,
                                onValueChange = { viewModel.updateCustomRoundRange(it, uiState.customEndRound) },
                                label = { Text("시작 회차") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = uiState.customEndRound,
                                onValueChange = { viewModel.updateCustomRoundRange(uiState.customStartRound, it) },
                                label = { Text("끝 회차") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            Button(onClick = { viewModel.applyCustomRoundRange() }) {
                                Text("적용")
                            }
                        }
                        uiState.customRangeError?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = LottoColors.DangerText,
                            )
                        }
                        Text("누적 구매 금액", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                        Text(uiState.totalPurchaseAmount.toWonLabel(), fontWeight = FontWeight.Bold)
                        Text("누적 당첨 금액", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                        Text(uiState.totalWinAmount.toWonLabel(), fontWeight = FontWeight.Bold)
                        Text("순이익(당첨-구매)", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                        Text(uiState.netProfitAmount.toWonLabel(), fontWeight = FontWeight.Bold)
                        Text("총 게임 수: ${uiState.totalGames}", style = MaterialTheme.typography.bodyMedium)
                        Text("당첨 게임 수: ${uiState.winningGames}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "※ 1·2등은 고정 추정 금액 기준",
                            style = MaterialTheme.typography.bodySmall,
                            color = LottoColors.TextMuted,
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "번호 구간 분포 히트맵",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        uiState.numberDistribution.forEach { bucket ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        bucket.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "${bucket.count}개 (${bucket.percent}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LottoColors.TextSecondary,
                                    )
                                }
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(
                                                color = LottoColors.Border.copy(alpha = 0.45f),
                                                shape = RoundedCornerShape(999.dp),
                                            ),
                                ) {
                                    if (bucket.count > 0) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(
                                                        fraction = (bucket.percent / 100f).coerceAtLeast(0.05f),
                                                    )
                                                    .height(8.dp)
                                                    .background(
                                                        color = LottoColors.Primary,
                                                        shape = RoundedCornerShape(999.dp),
                                                    ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "자주 나온 번호 TOP 6",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.topNumbers.forEach { LottoBall(number = it.value) }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "출처별 성과 비교",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        uiState.sourceStats.forEach { sourceStats ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                                colors = CardDefaults.cardColors(containerColor = LottoColors.Background),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        sourceStats.source.toSourceChipLabel(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LottoColors.Primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        "게임 ${sourceStats.totalGames} · 당첨 ${sourceStats.winningGames} (${sourceStats.winRatePercent}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LottoColors.TextSecondary,
                                    )
                                    Text(
                                        "ROI ${sourceStats.roiPercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LottoColors.TextSecondary,
                                    )
                                    Text(
                                        "구매 ${sourceStats.totalPurchaseAmount.toWonLabel()} / 당첨 ${sourceStats.totalWinAmount.toWonLabel()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LottoColors.TextSecondary,
                                    )
                                    Text(
                                        "순이익 ${sourceStats.netProfitAmount.toWonLabel()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            if (sourceStats.netProfitAmount >= 0) {
                                                LottoColors.SuccessText
                                            } else {
                                                LottoColors.DangerText
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "회차별 ROI 트렌드",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState.roiTrend.isEmpty()) {
                            Text(
                                "표시할 데이터가 없습니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LottoColors.TextMuted,
                            )
                        } else {
                            val maxAbsNet = uiState.roiTrend.maxOf { abs(it.netProfitAmount) }.coerceAtLeast(1L)
                            uiState.roiTrend.forEach { point ->
                                val netRatio = abs(point.netProfitAmount).toFloat() / maxAbsNet.toFloat()
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            "${point.round}회",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            "${point.netProfitAmount.toWonLabel()} (${point.roiPercent}%)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color =
                                                if (point.netProfitAmount >= 0) {
                                                    LottoColors.SuccessText
                                                } else {
                                                    LottoColors.DangerText
                                                },
                                        )
                                    }
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .background(
                                                    color = LottoColors.Border.copy(alpha = 0.45f),
                                                    shape = RoundedCornerShape(999.dp),
                                                ),
                                    ) {
                                        if (point.netProfitAmount != 0L) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth(fraction = netRatio.coerceAtLeast(0.05f))
                                                        .height(8.dp)
                                                        .background(
                                                            color =
                                                                if (point.netProfitAmount >= 0) {
                                                                    LottoColors.SuccessText
                                                                } else {
                                                                    LottoColors.DangerText
                                                                },
                                                            shape = RoundedCornerShape(999.dp),
                                                        ),
                                            )
                                        }
                                    }
                                    Text(
                                        "게임 ${point.totalGames} · 구매 ${point.totalPurchaseAmount.toWonLabel()} · 당첨 ${point.totalWinAmount.toWonLabel()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LottoColors.TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
