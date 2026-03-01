package com.weeklylotto.app.feature.result

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.DrawRank
import com.weeklylotto.app.domain.model.PrizeAmountPolicy
import com.weeklylotto.app.domain.service.AnalyticsActionValue
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.feature.common.OFFICIAL_PURCHASE_URL
import com.weeklylotto.app.feature.common.PURCHASE_REDIRECT_NOTICE_SEEN_KEY
import com.weeklylotto.app.feature.common.PURCHASE_REDIRECT_PREF_NAME
import com.weeklylotto.app.feature.common.PurchaseRedirectWindowStatus
import com.weeklylotto.app.feature.common.buildPurchaseRedirectNotice
import com.weeklylotto.app.feature.common.openExternalUrl
import com.weeklylotto.app.ui.component.BadgeTone
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.StatusBadge
import com.weeklylotto.app.ui.component.motionClickable
import com.weeklylotto.app.ui.format.toWonLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens
import com.weeklylotto.app.ui.theme.LottoTypeTokens
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("CyclomaticComplexMethod")
fun ResultScreen() {
    val analyticsLogger = AppGraph.analyticsLogger
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val purchaseRedirectPreferences =
        remember(context) {
            context.getSharedPreferences(PURCHASE_REDIRECT_PREF_NAME, Context.MODE_PRIVATE)
        }
    val purchaseRedirectNotice = remember { buildPurchaseRedirectNotice() }
    var showPurchaseNoticeDialog by remember { mutableStateOf(false) }
    var showPurchaseFallbackDialog by remember { mutableStateOf(false) }
    val viewModel =
        viewModel<ResultViewModel>(
            factory =
                SingleViewModelFactory {
                    ResultViewModel(
                        drawRepository = AppGraph.drawRepository,
                        ticketRepository = AppGraph.ticketRepository,
                        evaluator = AppGraph.resultEvaluator,
                        resultViewTracker = AppGraph.resultViewTracker,
                    )
                },
        )

    val uiState by viewModel.uiState.collectAsState()
    val openOfficialPurchase = {
        val opened = openExternalUrl(context = context, url = OFFICIAL_PURCHASE_URL)
        if (opened) {
            showPurchaseFallbackDialog = false
            analyticsLogger.log(
                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "result",
                        AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_OPEN_BROWSER,
                    ),
            )
        } else {
            analyticsLogger.log(
                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "result",
                        AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_FAIL,
                        AnalyticsParamKey.ERROR_TYPE to "external_open_failed",
                    ),
            )
            showPurchaseFallbackDialog = true
        }
    }
    var isRoundSheetOpen by remember { mutableStateOf(false) }
    var pendingRound by remember(uiState.selectedRound, isRoundSheetOpen) { mutableStateOf(uiState.selectedRound) }
    val openRoundSheet = {
        uiState.drawResult?.let { currentDraw ->
            pendingRound = uiState.selectedRound ?: currentDraw.round.number
            isRoundSheetOpen = true
        }
    }

    val drawResult = uiState.drawResult

    if (showPurchaseNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showPurchaseNoticeDialog = false },
            title = { Text("외부 페이지 이동 안내") },
            text = {
                val messageColor =
                    when (purchaseRedirectNotice.status) {
                        PurchaseRedirectWindowStatus.CLOSED -> Color(0xFFD32F2F)
                        PurchaseRedirectWindowStatus.CLOSING_SOON -> Color(0xFFEF6C00)
                        PurchaseRedirectWindowStatus.OPEN -> LottoColors.TextSecondary
                    }
                Text(
                    purchaseRedirectNotice.message,
                    color = messageColor,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        purchaseRedirectPreferences.edit()
                            .putBoolean(PURCHASE_REDIRECT_NOTICE_SEEN_KEY, true)
                            .apply()
                        analyticsLogger.log(
                            event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                            params =
                                mapOf(
                                    AnalyticsParamKey.SCREEN to "result",
                                    AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                                    AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_CONFIRM,
                                ),
                        )
                        showPurchaseNoticeDialog = false
                        openOfficialPurchase()
                    },
                ) {
                    Text("이동")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseNoticeDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    if (showPurchaseFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showPurchaseFallbackDialog = false },
            title = { Text("열기에 실패했어요") },
            text = {
                Text(
                    "외부 브라우저 실행에 실패했습니다. 링크를 복사하거나 다시 시도해 주세요.",
                    color = LottoColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openOfficialPurchase()
                        if (!showPurchaseFallbackDialog) {
                            Toast.makeText(context, "브라우저에서 열었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("브라우저로 열기")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(OFFICIAL_PURCHASE_URL))
                        analyticsLogger.log(
                            event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                            params =
                                mapOf(
                                    AnalyticsParamKey.SCREEN to "result",
                                    AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                                    AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_COPY_LINK,
                                ),
                        )
                        showPurchaseFallbackDialog = false
                        Toast.makeText(context, "링크를 복사했습니다.", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text("링크 복사")
                }
            },
        )
    }

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
                                .motionClickable {
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
                            analyticsLogger.log(
                                event = AnalyticsEvent.INTERACTION_SHEET_APPLY,
                                params =
                                    mapOf(
                                        AnalyticsParamKey.SCREEN to "result",
                                        AnalyticsParamKey.COMPONENT to "round_sheet",
                                        AnalyticsParamKey.ACTION to AnalyticsActionValue.APPLY,
                                        "selected_round" to (pendingRound?.toString() ?: ""),
                                    ),
                            )
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
                    analyticsLogger.log(
                        event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                        params =
                            mapOf(
                                AnalyticsParamKey.SCREEN to "result",
                                AnalyticsParamKey.COMPONENT to "open_round_sheet_top",
                                AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                            ),
                    )
                    openRoundSheet()
                } else {
                    analyticsLogger.log(
                        event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                        params =
                            mapOf(
                                AnalyticsParamKey.SCREEN to "result",
                                AnalyticsParamKey.COMPONENT to "refresh_top",
                                AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                            ),
                    )
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
                    Button(
                        onClick = {
                            analyticsLogger.log(
                                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                params =
                                    mapOf(
                                        AnalyticsParamKey.SCREEN to "result",
                                        AnalyticsParamKey.COMPONENT to "refresh_error",
                                        AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                    ),
                            )
                            viewModel.refresh()
                        },
                    ) {
                        Text("다시 시도")
                    }
                    if (uiState.selectedRound != null) {
                        TextButton(
                            onClick = {
                                analyticsLogger.log(
                                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                    params =
                                        mapOf(
                                            AnalyticsParamKey.SCREEN to "result",
                                            AnalyticsParamKey.COMPONENT to "load_latest_from_error",
                                            AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                        ),
                                )
                                viewModel.loadLatestFromError()
                            },
                        ) {
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
                            style = LottoTypeTokens.NumericTitle,
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
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .motionClickable {
                                analyticsLogger.log(
                                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                    params =
                                        mapOf(
                                            AnalyticsParamKey.SCREEN to "result",
                                            AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                                            AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_TAP,
                                        ),
                                )
                                val noticeSeen =
                                    purchaseRedirectPreferences.getBoolean(
                                        PURCHASE_REDIRECT_NOTICE_SEEN_KEY,
                                        false,
                                    )
                                if (noticeSeen) {
                                    openOfficialPurchase()
                                } else {
                                    showPurchaseNoticeDialog = true
                                }
                            },
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.padding(LottoDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "구매는 공식 홈페이지에서만 가능",
                            style = MaterialTheme.typography.bodySmall,
                            color = LottoColors.TextMuted,
                        )
                        Text(
                            text = "공식 홈페이지에서 구매하기",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LottoColors.PrimaryDark,
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
                        modifier =
                            Modifier.motionClickable {
                                analyticsLogger.log(
                                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                    params =
                                        mapOf(
                                            AnalyticsParamKey.SCREEN to "result",
                                            AnalyticsParamKey.COMPONENT to "open_round_sheet_list",
                                            AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                        ),
                                )
                                openRoundSheet()
                            },
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(LottoDimens.CardRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                    colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(LottoDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "당첨 ${uiState.winningCount}게임",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LottoColors.TextSecondary,
                        )
                        Text(
                            text = "예상 당첨금 합계 ${uiState.totalWinningAmount.toWonLabel()}",
                            style = LottoTypeTokens.NumericTitle,
                            fontWeight = FontWeight.Black,
                            color = LottoColors.TextPrimary,
                        )
                    }
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
                val estimatedPrize = PrizeAmountPolicy.amountFor(evaluated.result.rank)
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
                        Text(
                            text =
                                if (estimatedPrize > 0) {
                                    "예상 당첨금 ${estimatedPrize.toWonLabel()}"
                                } else {
                                    "예상 당첨금 없음"
                                },
                            color =
                                if (estimatedPrize > 0) {
                                    LottoColors.Primary
                                } else {
                                    LottoColors.TextMuted
                                },
                            style = LottoTypeTokens.NumericBody,
                            fontWeight = if (estimatedPrize > 0) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

private val errorTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
