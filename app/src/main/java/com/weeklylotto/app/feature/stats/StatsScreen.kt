package com.weeklylotto.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        }
    }
}
