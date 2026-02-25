package com.weeklylotto.app.feature.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.ui.component.BadgeTone
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.StatusBadge
import com.weeklylotto.app.ui.component.TicketCard
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("CyclomaticComplexMethod")
fun ManageScreen(
    onOpenQr: () -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenManualAdd: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenTicketDetail: (Long) -> Unit,
) {
    val viewModel =
        viewModel<ManageViewModel>(
            factory =
                SingleViewModelFactory {
                    ManageViewModel(ticketRepository = AppGraph.ticketRepository)
                },
        )
    val uiState by viewModel.uiState.collectAsState()
    val filteredTickets = viewModel.filteredTickets()
    val latestRound = uiState.tickets.maxOfOrNull { it.round.number } ?: 1
    val recent5Range = (latestRound - 4).coerceAtLeast(1)..latestRound
    val recent10Range = (latestRound - 9).coerceAtLeast(1)..latestRound

    if (uiState.isFabSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeFabSheet,
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("번호 추가", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        viewModel.closeFabSheet()
                        onOpenManualAdd()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("번호 직접 추가")
                }
                TextButton(
                    onClick = {
                        viewModel.closeFabSheet()
                        onOpenQr()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("QR 스캔")
                }
                TextButton(
                    onClick = {
                        viewModel.closeFabSheet()
                        onOpenImport()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("가져오기")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (uiState.isFilterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeFilterSheet,
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("필터", style = MaterialTheme.typography.titleMedium)
                Text("상태", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TicketStatus.entries.forEach { status ->
                        FilterChip(
                            selected = status in uiState.filter.statuses,
                            onClick = { viewModel.toggleStatusFilter(status) },
                            label = { Text(status.toBadgeLabel()) },
                        )
                    }
                }
                Text("회차", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filter.roundRange == null,
                        onClick = { viewModel.setRoundRange(null) },
                        label = { Text("전체") },
                    )
                    FilterChip(
                        selected = uiState.filter.roundRange == recent5Range,
                        onClick = { viewModel.setRoundRange(recent5Range) },
                        label = { Text("최근 5회") },
                    )
                    FilterChip(
                        selected = uiState.filter.roundRange == recent10Range,
                        onClick = { viewModel.setRoundRange(recent10Range) },
                        label = { Text("최근 10회") },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = viewModel::clearFilter) { Text("초기화") }
                    Button(onClick = viewModel::closeFilterSheet) { Text("적용") }
                }
            }
        }
    }

    if (uiState.isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("삭제할까요?") },
            text = { Text("선택한 ${uiState.selectedIds.size}개의 번호가 보관함에서 제거됩니다.") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteSelected) {
                    Text("삭제", color = LottoColors.DangerText)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDeleteDialog) { Text("취소") } },
        )
    }

    Scaffold(
        topBar = {
            LottoTopAppBar(
                title = "번호관리",
                rightActionText = if (uiState.editMode) "완료" else "필터",
                onRightClick = {
                    if (uiState.editMode) {
                        viewModel.toggleEditMode()
                    } else {
                        viewModel.openFilterSheet()
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openFabSheet,
                containerColor = LottoColors.Accent,
                contentColor = LottoColors.TextPrimary,
                modifier = Modifier.size(LottoDimens.FabSize),
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "추가",
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(LottoColors.Background),
        ) {
            TabRow(selectedTabIndex = uiState.tab.ordinal) {
                ManageTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.tab == tab,
                        onClick = { viewModel.setTab(tab) },
                        text = {
                            Text(
                                text =
                                    when (tab) {
                                        ManageTab.WEEK -> "이번주"
                                        ManageTab.VAULT -> "보관함"
                                        ManageTab.SCAN -> "스캔내역"
                                    },
                            )
                        },
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LottoDimens.ScreenPadding, vertical = LottoDimens.CardGap),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("저장된 번호", style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = viewModel::toggleEditMode,
                ) {
                    Text(if (uiState.editMode) "완료" else "편집")
                }
            }

            if (uiState.editMode) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LottoDimens.ScreenPadding, vertical = 8.dp)
                            .background(LottoColors.Border.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${uiState.selectedIds.size}개 선택")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onOpenGenerator) { Text("이동") }
                        TextButton(onClick = viewModel::requestDeleteSelected) { Text("삭제") }
                    }
                }
            }

            HorizontalDivider(color = LottoColors.Border)

            if (filteredTickets.isEmpty()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("저장된 번호가 없습니다.", color = LottoColors.TextSecondary)
                    if (uiState.tab == ManageTab.SCAN) {
                        Button(onClick = onOpenQr) { Text("스캔 열기") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(LottoDimens.CardGap),
                    contentPadding = PaddingValues(LottoDimens.ScreenPadding),
                ) {
                    items(filteredTickets, key = { it.id }) { bundle ->
                        val title = "${bundle.round.number}회 ${bundle.source.toSourceLabel()}"
                        val meta = "${bundle.games.size}게임 · ${bundle.createdAt.toDisplayDate()}"

                        Column {
                            if (uiState.editMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = bundle.id in uiState.selectedIds,
                                        onCheckedChange = { viewModel.toggleSelection(bundle.id) },
                                    )
                                    Text("선택", color = LottoColors.TextMuted)
                                }
                            }
                            TicketCard(
                                title = title,
                                numbers = bundle.games.firstOrNull()?.numbers?.map { it.value }.orEmpty(),
                                badge = {
                                    StatusBadge(
                                        label = bundle.status.toBadgeLabel(),
                                        tone = bundle.status.toBadgeTone(),
                                    )
                                },
                                meta = meta,
                                onClick = {
                                    if (uiState.editMode) {
                                        viewModel.toggleSelection(bundle.id)
                                    } else {
                                        onOpenTicketDetail(bundle.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun TicketStatus.toBadgeLabel(): String =
    when (this) {
        TicketStatus.WIN -> "당첨"
        TicketStatus.LOSE -> "낙첨"
        TicketStatus.PENDING -> "대기"
    }

private fun TicketStatus.toBadgeTone(): BadgeTone =
    when (this) {
        TicketStatus.WIN -> BadgeTone.Success
        TicketStatus.LOSE -> BadgeTone.Neutral
        TicketStatus.PENDING -> BadgeTone.Accent
    }

private fun TicketSource.toSourceLabel(): String =
    when (this) {
        TicketSource.GENERATED -> "자동"
        TicketSource.QR_SCAN -> "QR"
        TicketSource.MANUAL -> "수동"
    }

private fun java.time.Instant.toDisplayDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return formatter.format(atZone(ZoneId.systemDefault()))
}
