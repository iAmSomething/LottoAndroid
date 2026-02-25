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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.LottoBall
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory

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

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00796B))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text("통계", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("조회 기간", color = Color(0xFF757575))
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
                        Text("누적 구매 금액", color = Color(0xFF757575))
                        Text("${uiState.totalPurchaseAmount}원", fontWeight = FontWeight.Bold)
                        Text("누적 당첨 금액", color = Color(0xFF757575))
                        Text("${uiState.totalWinAmount}원", fontWeight = FontWeight.Bold)
                        Text("순이익(당첨-구매)", color = Color(0xFF757575))
                        Text("${uiState.netProfitAmount}원", fontWeight = FontWeight.Bold)
                        Text("총 게임 수: ${uiState.totalGames}")
                        Text("당첨 게임 수: ${uiState.winningGames}")
                        Text("※ 1·2등은 고정 추정 금액 기준", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("자주 나온 번호 TOP 6", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.topNumbers.forEach { LottoBall(number = it.value) }
                        }
                    }
                }
            }
        }
    }
}
