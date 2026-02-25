package com.weeklylotto.app.feature.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.TicketCard
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors

@Composable
fun TicketDetailScreen(
    ticketId: Long,
    onBack: () -> Unit,
) {
    val viewModel =
        viewModel<ManageViewModel>(
            factory =
                SingleViewModelFactory {
                    ManageViewModel(ticketRepository = AppGraph.ticketRepository)
                },
        )
    val uiState by viewModel.uiState.collectAsState()
    val ticket = uiState.tickets.firstOrNull { it.id == ticketId }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LottoColors.Background),
    ) {
        LottoTopAppBar(
            title = "${ticket?.round?.number ?: "-"}회 상세",
            rightActionText = "닫기",
            onRightClick = onBack,
        )

        if (ticket == null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("티켓을 찾을 수 없습니다.", color = LottoColors.TextSecondary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("게임", style = MaterialTheme.typography.titleMedium)
            }
            items(ticket.games) { game ->
                TicketCard(
                    title = "${game.slot} 게임",
                    numbers = game.numbers.map { it.value },
                    meta = "모드: ${game.mode}",
                )
            }
        }
    }
}
