package com.weeklylotto.app.feature.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.StatusBadge
import com.weeklylotto.app.ui.component.TicketCard
import com.weeklylotto.app.ui.format.toBadgeTone
import com.weeklylotto.app.ui.format.toSourceChipLabel
import com.weeklylotto.app.ui.format.toStatusLabel
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
    val activeFilterCount = uiState.filter.statuses.size + if (uiState.filter.roundRange != null) 1 else 0
    val availableStatuses = remember(uiState.tab) { filterStatusesForTab(uiState.tab) }
    val scanSummary = if (uiState.tab == ManageTab.SCAN) viewModel.scanSummary() else null
    val latestRound = uiState.tickets.maxOfOrNull { it.round.number } ?: 1
    val recent5Range = (latestRound - 4).coerceAtLeast(1)..latestRound
    val recent10Range = (latestRound - 9).coerceAtLeast(1)..latestRound
    val filterSummary =
        remember(uiState.filter, recent5Range, recent10Range) {
            buildFilterSummary(
                filter = uiState.filter,
                recent5Range = recent5Range,
                recent10Range = recent10Range,
            )
        }
    var customRangeStart by remember(uiState.isFilterSheetOpen, uiState.filter.roundRange) {
        mutableStateOf(uiState.filter.roundRange?.first?.toString().orEmpty())
    }
    var customRangeEnd by remember(uiState.isFilterSheetOpen, uiState.filter.roundRange) {
        mutableStateOf(uiState.filter.roundRange?.last?.toString().orEmpty())
    }
    var customRangeError by remember(uiState.isFilterSheetOpen) { mutableStateOf<String?>(null) }

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
                if (filterSummary != null) {
                    Text(
                        text = "적용 중: $filterSummary",
                        style = MaterialTheme.typography.bodySmall,
                        color = LottoColors.TextMuted,
                    )
                }
                Text("상태", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = uiState.filter.statuses.isEmpty(),
                        onClick = viewModel::clearStatusFilters,
                        label = { Text("전체") },
                    )
                    availableStatuses.forEach { status ->
                        FilterChip(
                            selected = status in uiState.filter.statuses,
                            onClick = { viewModel.toggleStatusFilter(status) },
                            label = { Text(status.toStatusLabel()) },
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
                Text("직접 입력", style = MaterialTheme.typography.bodySmall, color = LottoColors.TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customRangeStart,
                        onValueChange = {
                            customRangeStart = it.filter(Char::isDigit).take(4)
                            customRangeError = null
                        },
                        label = { Text("시작 회차") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = customRangeEnd,
                        onValueChange = {
                            customRangeEnd = it.filter(Char::isDigit).take(4)
                            customRangeError = null
                        },
                        label = { Text("끝 회차") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
                customRangeError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = LottoColors.DangerText,
                    )
                }
                Button(
                    onClick = {
                        val start = customRangeStart.toIntOrNull()
                        val end = customRangeEnd.toIntOrNull()
                        val errorMessage =
                            when {
                                customRangeStart.isBlank() || customRangeEnd.isBlank() -> "시작/끝 회차를 모두 입력하세요."
                                start == null || end == null -> "회차는 숫자로 입력하세요."
                                start <= 0 || end <= 0 -> "회차는 1 이상이어야 합니다."
                                start > end -> "시작 회차가 끝 회차보다 클 수 없습니다."
                                else -> null
                            }
                        if (errorMessage != null) {
                            customRangeError = errorMessage
                        } else {
                            viewModel.setRoundRange(start!!..end!!)
                            customRangeError = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("직접 입력 범위 적용")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = {
                            viewModel.clearFilter()
                            customRangeStart = ""
                            customRangeEnd = ""
                            customRangeError = null
                        },
                    ) { Text("초기화") }
                    Button(onClick = viewModel::closeFilterSheet) { Text("적용") }
                }
            }
        }
    }

    if (uiState.isSortSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeSortSheet,
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("정렬", style = MaterialTheme.typography.titleMedium)
                ManageSort.entries.forEach { sort ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = uiState.sort == sort,
                            onClick = { viewModel.setSort(sort) },
                            label = { Text(sort.toSortLabel()) },
                        )
                        Text(
                            text = sort.toSortDescription(),
                            style = MaterialTheme.typography.bodySmall,
                            color = LottoColors.TextMuted,
                        )
                    }
                }
                Button(
                    onClick = viewModel::closeSortSheet,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("닫기")
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

    if (uiState.isMoveSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeMoveSheet,
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("이동", style = MaterialTheme.typography.titleMedium)
                Text(
                    "선택한 ${uiState.selectedIds.size}개를 보관함으로 이동합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LottoColors.TextMuted,
                )
                Button(
                    onClick = viewModel::moveSelectedToVault,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("보관함으로 이동")
                }
                TextButton(
                    onClick = viewModel::closeMoveSheet,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("취소")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LottoTopAppBar(
                title = "번호관리",
                rightActionText =
                    if (uiState.editMode) {
                        "완료"
                    } else if (activeFilterCount > 0) {
                        "필터 $activeFilterCount"
                    } else {
                        "필터"
                    },
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = viewModel::openSortSheet) {
                        Text(uiState.sort.toSortShortLabel())
                    }
                    TextButton(
                        onClick = viewModel::toggleEditMode,
                    ) {
                        Text(if (uiState.editMode) "완료" else "편집")
                    }
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
                        TextButton(onClick = viewModel::openMoveSheet) { Text("이동") }
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
                    if (uiState.tab == ManageTab.VAULT) {
                        item {
                            VaultSummaryCard(
                                totalCount = filteredTickets.size,
                                savedCount = filteredTickets.count { it.status == TicketStatus.SAVED },
                                winningCount = filteredTickets.count { it.status == TicketStatus.WIN },
                            )
                        }
                    }
                    if (uiState.tab == ManageTab.SCAN && scanSummary != null) {
                        item {
                            ScanSummaryCard(
                                totalCount = scanSummary.totalCount,
                                currentRoundCount = scanSummary.currentRoundCount,
                                latestRound = scanSummary.latestRound,
                            )
                        }
                    }
                    items(filteredTickets, key = { it.id }) { bundle ->
                        val title =
                            if (uiState.tab == ManageTab.SCAN) {
                                "${bundle.round.number}회 QR 스캔"
                            } else {
                                "${bundle.round.number}회 ${bundle.source.toSourceChipLabel()}"
                            }
                        val meta =
                            if (uiState.tab == ManageTab.SCAN) {
                                "스캔 등록 ${bundle.createdAt.toDisplayDateTime()} · ${bundle.games.size}게임"
                            } else {
                                "${bundle.games.size}게임 · ${bundle.createdAt.toDisplayDate()}"
                            }

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
                                        label = bundle.status.toStatusLabel(),
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

@Composable
private fun VaultSummaryCard(
    totalCount: Int,
    savedCount: Int,
    winningCount: Int,
) {
    Card(
        shape = RoundedCornerShape(LottoDimens.CardRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
        colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
    ) {
        Column(
            modifier = Modifier.padding(LottoDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("보관함 요약", style = MaterialTheme.typography.titleSmall)
            Text("총 ${totalCount}건", color = LottoColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text("보관 상태 ${savedCount}건", color = LottoColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                "당첨 상태 ${winningCount}건",
                color = LottoColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ScanSummaryCard(
    totalCount: Int,
    currentRoundCount: Int,
    latestRound: Int?,
) {
    Card(
        shape = RoundedCornerShape(LottoDimens.CardRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
        colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
    ) {
        Column(
            modifier = Modifier.padding(LottoDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("스캔내역 요약", style = MaterialTheme.typography.titleSmall)
            latestRound?.let { round ->
                Text(
                    "최신 스캔 회차 ${round}회",
                    color = LottoColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("총 ${totalCount}건", color = LottoColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                "이번주 스캔 ${currentRoundCount}건",
                color = LottoColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun java.time.Instant.toDisplayDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return formatter.format(atZone(ZoneId.systemDefault()))
}

private fun java.time.Instant.toDisplayDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
    return formatter.format(atZone(ZoneId.systemDefault()))
}

private fun filterStatusesForTab(tab: ManageTab): List<TicketStatus> =
    when (tab) {
        ManageTab.WEEK -> listOf(TicketStatus.PENDING, TicketStatus.WIN, TicketStatus.LOSE)
        ManageTab.VAULT -> listOf(TicketStatus.SAVED, TicketStatus.WIN, TicketStatus.LOSE, TicketStatus.PENDING)
        ManageTab.SCAN -> listOf(TicketStatus.PENDING, TicketStatus.WIN, TicketStatus.LOSE, TicketStatus.SAVED)
    }

private fun buildFilterSummary(
    filter: ManageFilter,
    recent5Range: IntRange,
    recent10Range: IntRange,
): String? {
    val parts = mutableListOf<String>()
    if (filter.statuses.isNotEmpty()) {
        parts += filter.statuses.joinToString("/") { it.toStatusLabel() }
    }
    filter.roundRange?.let { range ->
        val label =
            when (range) {
                recent5Range -> "최근 5회"
                recent10Range -> "최근 10회"
                else -> "${range.first}~${range.last}회"
            }
        parts += label
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
}

private fun ManageSort.toSortShortLabel(): String =
    when (this) {
        ManageSort.LATEST_CREATED -> "최신순"
        ManageSort.OLDEST_CREATED -> "오래된순"
        ManageSort.ROUND_DESC -> "회차순"
    }

private fun ManageSort.toSortLabel(): String =
    when (this) {
        ManageSort.LATEST_CREATED -> "등록 최신순"
        ManageSort.OLDEST_CREATED -> "등록 오래된순"
        ManageSort.ROUND_DESC -> "회차 높은순"
    }

private fun ManageSort.toSortDescription(): String =
    when (this) {
        ManageSort.LATEST_CREATED -> "최근 등록한 번호부터 보여줍니다."
        ManageSort.OLDEST_CREATED -> "먼저 등록한 번호부터 보여줍니다."
        ManageSort.ROUND_DESC -> "회차가 높은 번호를 먼저 보여줍니다."
    }
