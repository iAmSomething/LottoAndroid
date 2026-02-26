package com.weeklylotto.app.feature.manage

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.TicketCard
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("게임", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = {
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
                        },
                    ) {
                        Text("공유")
                    }
                }
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
            "출처: ${ticket.source.toKoreanLabel()}",
            "상태: ${ticket.status.toKoreanLabel()}",
            "등록일: $createdAtText",
        )

    val gameLines =
        ticket.games.map { game ->
            val numbers = game.numbers.joinToString(", ") { it.value.toString().padStart(2, '0') }
            "${game.slot} 게임(${game.mode.toKoreanLabel()}): $numbers"
        }

    return (headerLines + "" + gameLines).joinToString(separator = "\n")
}

private fun TicketSource.toKoreanLabel(): String =
    when (this) {
        TicketSource.GENERATED -> "번호 생성"
        TicketSource.QR_SCAN -> "QR 스캔"
        TicketSource.MANUAL -> "수동 입력"
    }

private fun TicketStatus.toKoreanLabel(): String =
    when (this) {
        TicketStatus.PENDING -> "대기"
        TicketStatus.WIN -> "당첨"
        TicketStatus.LOSE -> "낙첨"
    }

private fun GameMode.toKoreanLabel(): String =
    when (this) {
        GameMode.AUTO -> "자동"
        GameMode.MANUAL -> "수동"
        GameMode.SEMI_AUTO -> "반자동"
    }
