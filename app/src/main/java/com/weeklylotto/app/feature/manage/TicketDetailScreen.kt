package com.weeklylotto.app.feature.manage

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.TicketCard
import com.weeklylotto.app.ui.format.toModeLabel
import com.weeklylotto.app.ui.format.toSourceDisplayLabel
import com.weeklylotto.app.ui.format.toStatusLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TicketDetailScreen(
    ticketId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel =
        viewModel<ManageViewModel>(
            factory =
                SingleViewModelFactory {
                    ManageViewModel(ticketRepository = AppGraph.ticketRepository)
                },
        )
    val uiState by viewModel.uiState.collectAsState()
    val ticket = uiState.tickets.firstOrNull { it.id == ticketId }

    LaunchedEffect(uiState.feedbackMessage) {
        val message = uiState.feedbackMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.clearFeedbackMessage()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LottoColors.Background),
    ) {
        LottoTopAppBar(
            title = "${ticket?.round?.number ?: "-"}회 상세",
            rightActionText = "공유",
            onRightClick = {
                ticket?.let { shareTicket(context, it) }
            },
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
                    meta = "모드: ${game.mode.toModeLabel()}",
                )
            }
            item {
                Button(
                    onClick = { viewModel.copyTicketToCurrentRound(ticketId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("이번주로 복사")
                }
            }
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

private fun shareTicket(
    context: android.content.Context,
    ticket: TicketBundle,
) {
    val shareText = buildTicketShareText(ticket)
    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, "티켓 공유"))
    }.onFailure {
        Toast.makeText(context, "공유 가능한 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
}

internal fun buildTicketShareText(
    ticket: TicketBundle,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val drawDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val createdAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val createdAtText = ticket.createdAt.atZone(zoneId).format(createdAtFormatter)

    val headerLines =
        listOf(
            "매주로또 티켓 공유",
            "회차: 제 ${ticket.round.number}회 (추첨일 ${ticket.round.drawDate.format(drawDateFormatter)})",
            "출처: ${ticket.source.toSourceDisplayLabel()}",
            "상태: ${ticket.status.toStatusLabel()}",
            "등록일: $createdAtText",
        )

    val gameLines =
        ticket.games.map { game ->
            val numbers = game.numbers.joinToString(", ") { it.value.toString().padStart(2, '0') }
            "${game.slot} 게임(${game.mode.toModeLabel()}): $numbers"
        }

    return (headerLines + "" + gameLines).joinToString(separator = "\n")
}
