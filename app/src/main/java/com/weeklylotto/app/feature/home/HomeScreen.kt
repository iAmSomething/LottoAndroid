package com.weeklylotto.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.StatusBadge
import com.weeklylotto.app.ui.component.TicketCard
import com.weeklylotto.app.ui.format.toBadgeTone
import com.weeklylotto.app.ui.format.toSourceChipLabel
import com.weeklylotto.app.ui.format.toStatusLabel
import com.weeklylotto.app.ui.format.toWonLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    onClickGenerator: () -> Unit,
    onClickManage: () -> Unit,
    onClickResult: () -> Unit,
    onClickSettings: () -> Unit,
    onClickQr: () -> Unit,
) {
    val viewModel =
        viewModel<HomeViewModel>(
            factory =
                SingleViewModelFactory {
                    HomeViewModel(
                        ticketRepository = AppGraph.ticketRepository,
                        drawRepository = AppGraph.drawRepository,
                        resultEvaluator = AppGraph.resultEvaluator,
                        resultViewTracker = AppGraph.resultViewTracker,
                    )
                },
        )
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "매주로또",
            rightActionText = "설정",
            onRightClick = onClickSettings,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    horizontal = LottoDimens.ScreenPadding,
                    vertical = LottoDimens.SectionGap,
                ),
            verticalArrangement = Arrangement.spacedBy(LottoDimens.SectionGap),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(LottoDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "이번 주 토요일 추첨",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LottoColors.TextMuted,
                        )
                        Text(
                            text = "제 ${uiState.currentRound}회 로또 6/45",
                            style = MaterialTheme.typography.titleLarge,
                            color = LottoColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                        )
                        Box(
                            modifier =
                                Modifier
                                    .background(color = LottoColors.Border, shape = CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = "D-${uiState.dDay} · ${uiState.drawDate.dayOfWeek.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.KOREAN,
                                )} 20:45",
                                style = MaterialTheme.typography.bodySmall,
                                color = LottoColors.TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(LottoDimens.CardGap)) {
                    Card(
                        modifier = Modifier.weight(1f).clickable { onClickQr() },
                        shape = RoundedCornerShape(LottoDimens.CardRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = LottoColors.PrimaryDark),
                    ) {
                        Column(
                            modifier = Modifier.padding(LottoDimens.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "QR 스캔",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = "종이 복권 등록",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f).clickable { onClickGenerator() },
                        shape = RoundedCornerShape(LottoDimens.CardRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = LottoColors.Accent),
                    ) {
                        Column(
                            modifier = Modifier.padding(LottoDimens.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "번호 생성",
                                color = LottoColors.TextPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = "스마트 랜덤 추출",
                                color = LottoColors.TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            if (uiState.hasUnseenResult && uiState.unseenRound != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onClickResult() },
                        shape = RoundedCornerShape(LottoDimens.CardRadius),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                        colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(LottoDimens.ScreenPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "미확인 결과",
                                    color = LottoColors.Primary,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    text = "${uiState.unseenRound}회 결과를 아직 확인하지 않았습니다.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LottoColors.TextSecondary,
                                )
                            }
                            Text(
                                text = "확인",
                                color = LottoColors.Primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }

            uiState.weeklyReport?.let { report ->
                item {
                    Card(
                        shape = RoundedCornerShape(LottoDimens.CardRadius),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                        colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                    ) {
                        Column(
                            modifier = Modifier.padding(LottoDimens.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "${report.round}회 주간 리포트",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = "구매 ${report.totalGames}게임 · 당첨 ${report.winningGames}게임",
                                style = MaterialTheme.typography.bodySmall,
                                color = LottoColors.TextSecondary,
                            )
                            Text(
                                text = "구매 ${report.totalPurchaseAmount.toWonLabel()} / 당첨 ${report.totalWinningAmount.toWonLabel()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LottoColors.TextSecondary,
                            )
                            Text(
                                text = "순이익 ${report.netProfitAmount.toWonLabel()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (report.netProfitAmount >= 0) LottoColors.Primary else LottoColors.TextPrimary,
                                fontWeight = FontWeight.Black,
                            )
                        }
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
                        text = "이번 주 구매 번호",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "전체보기",
                        color = LottoColors.Primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable { onClickManage() },
                    )
                }
            }

            if (uiState.bundles.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(LottoDimens.CardRadius),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                        colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                    ) {
                        Column(
                            modifier = Modifier.padding(LottoDimens.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("저장된 번호가 없습니다.", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "번호 생성 화면에서 이번 주 번호를 저장하세요.",
                                color = LottoColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            } else {
                items(uiState.bundles.take(2), key = { it.id }) { bundle ->
                    val firstGame = bundle.games.firstOrNull()
                    TicketCard(
                        title = "${firstGame?.slot?.name ?: "A"} 게임 (${bundle.source.toSourceChipLabel()})",
                        numbers = firstGame?.numbers?.map { it.value }.orEmpty(),
                        badge = {
                            StatusBadge(
                                label = bundle.status.toStatusLabel(),
                                tone = bundle.status.toBadgeTone(),
                            )
                        },
                        meta = "${bundle.round.number}회 · ${bundle.games.size}게임",
                        onClick = onClickManage,
                    )
                }
            }
        }
    }
}
