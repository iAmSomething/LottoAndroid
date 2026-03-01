package com.weeklylotto.app.feature.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.weeklylotto.app.domain.service.AnalyticsActionValue
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.feature.common.OFFICIAL_PURCHASE_URL
import com.weeklylotto.app.feature.common.PURCHASE_REDIRECT_NOTICE_SEEN_KEY
import com.weeklylotto.app.feature.common.PURCHASE_REDIRECT_PREF_NAME
import com.weeklylotto.app.feature.common.PurchaseRedirectWindowStatus
import com.weeklylotto.app.feature.common.buildPurchaseRedirectNotice
import com.weeklylotto.app.feature.common.openExternalUrl
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.StatusBadge
import com.weeklylotto.app.ui.component.TicketCard
import com.weeklylotto.app.ui.component.motionClickable
import com.weeklylotto.app.ui.format.toBadgeTone
import com.weeklylotto.app.ui.format.toSourceChipLabel
import com.weeklylotto.app.ui.format.toStatusLabel
import com.weeklylotto.app.ui.format.toWonLabel
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens
import com.weeklylotto.app.ui.theme.LottoTypeTokens
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onClickGenerator: () -> Unit,
    onClickManage: () -> Unit,
    onClickResult: () -> Unit,
    onClickSettings: () -> Unit,
    onClickQr: () -> Unit,
) {
    val analyticsLogger = AppGraph.analyticsLogger
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lottoStoreSearchUrl = remember { buildLottoStoreSearchUrl() }
    val purchaseRedirectPreferences =
        remember(context) {
            context.getSharedPreferences(PURCHASE_REDIRECT_PREF_NAME, Context.MODE_PRIVATE)
        }
    val purchaseRedirectNotice = remember { buildPurchaseRedirectNotice() }
    var showPurchaseNoticeDialog by remember { mutableStateOf(false) }
    var showPurchaseFallbackDialog by remember { mutableStateOf(false) }
    var showStoreSearchFallbackDialog by remember { mutableStateOf(false) }
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
    val openOfficialPurchase = {
        val opened = openExternalUrl(context = context, url = OFFICIAL_PURCHASE_URL)
        if (opened) {
            showPurchaseFallbackDialog = false
            analyticsLogger.log(
                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "home",
                        AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_OPEN_BROWSER,
                    ),
            )
        } else {
            analyticsLogger.log(
                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "home",
                        AnalyticsParamKey.COMPONENT to "purchase_redirect_cta",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.PURCHASE_REDIRECT_FAIL,
                        AnalyticsParamKey.ERROR_TYPE to "external_open_failed",
                    ),
            )
            showPurchaseFallbackDialog = true
        }
    }
    val openNearbyStoreSearch = {
        val opened = openExternalUrl(context = context, url = lottoStoreSearchUrl)
        if (opened) {
            showStoreSearchFallbackDialog = false
            analyticsLogger.log(
                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "home",
                        AnalyticsParamKey.COMPONENT to "lotto_store_search_cta",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.LOCATION_STORE_SEARCH_OPEN_MAP,
                    ),
            )
        } else {
            analyticsLogger.log(
                event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "home",
                        AnalyticsParamKey.COMPONENT to "lotto_store_search_cta",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.LOCATION_STORE_SEARCH_FAIL,
                        AnalyticsParamKey.ERROR_TYPE to "map_open_failed",
                    ),
            )
            showStoreSearchFallbackDialog = true
        }
    }

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
                                    AnalyticsParamKey.SCREEN to "home",
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
                                    AnalyticsParamKey.SCREEN to "home",
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

    if (showStoreSearchFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showStoreSearchFallbackDialog = false },
            title = { Text("지도 열기에 실패했어요") },
            text = {
                Text(
                    "지도를 열지 못했습니다. 링크를 복사하거나 다시 시도해 주세요.",
                    color = LottoColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openNearbyStoreSearch()
                        if (!showStoreSearchFallbackDialog) {
                            Toast.makeText(context, "지도 앱에서 열었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("다시 열기")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(lottoStoreSearchUrl))
                        analyticsLogger.log(
                            event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                            params =
                                mapOf(
                                    AnalyticsParamKey.SCREEN to "home",
                                    AnalyticsParamKey.COMPONENT to "lotto_store_search_cta",
                                    AnalyticsParamKey.ACTION to AnalyticsActionValue.LOCATION_STORE_SEARCH_COPY_LINK,
                                ),
                        )
                        showStoreSearchFallbackDialog = false
                        Toast.makeText(context, "지도 링크를 복사했습니다.", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text("링크 복사")
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "매주로또",
            rightActionText = "설정",
            onRightClick = {
                analyticsLogger.log(
                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                    params =
                        mapOf(
                            AnalyticsParamKey.SCREEN to "home",
                            AnalyticsParamKey.COMPONENT to "settings",
                            AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                        ),
                )
                onClickSettings()
            },
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
                            style = LottoTypeTokens.NumericTitle,
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
                        modifier =
                            Modifier
                                .weight(1f)
                                .motionClickable {
                                    analyticsLogger.log(
                                        event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                        params =
                                            mapOf(
                                                AnalyticsParamKey.SCREEN to "home",
                                                AnalyticsParamKey.COMPONENT to "cta_qr_scan",
                                                AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                            ),
                                    )
                                    onClickQr()
                                },
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
                        modifier =
                            Modifier
                                .weight(1f)
                                .motionClickable {
                                    analyticsLogger.log(
                                        event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                        params =
                                            mapOf(
                                                AnalyticsParamKey.SCREEN to "home",
                                                AnalyticsParamKey.COMPONENT to "cta_generate_numbers",
                                                AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                            ),
                                    )
                                    onClickGenerator()
                                },
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
                                            AnalyticsParamKey.SCREEN to "home",
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
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .motionClickable {
                                analyticsLogger.log(
                                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                    params =
                                        mapOf(
                                            AnalyticsParamKey.SCREEN to "home",
                                            AnalyticsParamKey.COMPONENT to "lotto_store_search_cta",
                                            AnalyticsParamKey.ACTION to AnalyticsActionValue.LOCATION_STORE_SEARCH_TAP,
                                        ),
                                )
                                openNearbyStoreSearch()
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
                            text = "근처 판매점 찾기",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LottoColors.PrimaryDark,
                        )
                        Text(
                            text = "지도 앱에서 주변 로또 판매점을 바로 검색합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LottoColors.TextMuted,
                        )
                    }
                }
            }

            if (uiState.hasUnseenResult && uiState.unseenRound != null) {
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
                                                AnalyticsParamKey.SCREEN to "home",
                                                AnalyticsParamKey.COMPONENT to "unseen_result_card",
                                                AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                            ),
                                    )
                                    onClickResult()
                                },
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "구매 ${report.totalGames}게임 · 당첨 ${report.winningGames}게임",
                                style = MaterialTheme.typography.bodySmall,
                                color = LottoColors.TextSecondary,
                            )
                            Text(
                                text =
                                    "당첨률 ${report.winningRatePercent}% · 결과 " +
                                        if (report.resultViewed) {
                                            "확인 완료"
                                        } else {
                                            "미확인"
                                        },
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
                                style = LottoTypeTokens.NumericBody,
                                color = if (report.netProfitAmount >= 0) LottoColors.Primary else LottoColors.TextPrimary,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }

            if (uiState.routineHistory.isNotEmpty()) {
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
                                text = "최근 8주 루틴 히스토리",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            uiState.routineHistory.forEach { history ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${history.round}회",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LottoColors.TextSecondary,
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text =
                                                if (history.purchasedGames > 0) {
                                                    "구매 ${history.purchasedGames}게임"
                                                } else {
                                                    "미구매"
                                                },
                                            style = MaterialTheme.typography.labelSmall,
                                            color =
                                                if (history.purchasedGames > 0) {
                                                    LottoColors.Primary
                                                } else {
                                                    LottoColors.TextMuted
                                                },
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = if (history.resultViewed) "결과 확인" else "미확인",
                                            style = MaterialTheme.typography.labelSmall,
                                            color =
                                                if (history.resultViewed) {
                                                    LottoColors.PrimaryDark
                                                } else {
                                                    LottoColors.TextMuted
                                                },
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
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
                        modifier =
                            Modifier.motionClickable {
                                analyticsLogger.log(
                                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                    params =
                                        mapOf(
                                            AnalyticsParamKey.SCREEN to "home",
                                            AnalyticsParamKey.COMPONENT to "view_all_tickets",
                                            AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                        ),
                                )
                                onClickManage()
                            },
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
                    Column(modifier = Modifier.animateItem()) {
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
                            onClick = {
                                analyticsLogger.log(
                                    event = AnalyticsEvent.INTERACTION_CTA_PRESS,
                                    params =
                                        mapOf(
                                            AnalyticsParamKey.SCREEN to "home",
                                            AnalyticsParamKey.COMPONENT to "ticket_card",
                                            AnalyticsParamKey.ACTION to AnalyticsActionValue.CLICK,
                                        ),
                                )
                                onClickManage()
                            },
                        )
                    }
                }
            }
        }
    }
}

internal fun buildLottoStoreSearchUrl(query: String = LOTTO_STORE_SEARCH_QUERY): String {
    val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
    return "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
}

private const val LOTTO_STORE_SEARCH_QUERY = "로또 판매점"
