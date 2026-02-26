package com.weeklylotto.wear

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.remote.interactions.RemoteActivityHelper
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors

private const val APP_SCHEME = "weeklylotto"
private const val APP_ROUTE_QR_SCAN = "qr_scan"
private const val APP_ROUTE_RESULT = "result"
private const val APP_ROUTE_SETTINGS = "settings"

@Composable
fun WeeklyLottoWearApp() {
    val handoffLauncher = rememberPhoneHandoffLauncher()
    val navController = rememberSwipeDismissableNavController()
    var purchaseReminderEnabled by rememberSaveable { mutableStateOf(true) }
    var resultReminderEnabled by rememberSaveable { mutableStateOf(true) }
    var hapticEnabled by rememberSaveable { mutableStateOf(true) }
    var handoffStatusText by rememberSaveable { mutableStateOf<String?>(null) }

    MaterialTheme {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = WearDestination.Home.route,
        ) {
            composable(WearDestination.Home.route) {
                WearHomeScreen(
                    statusText = handoffStatusText,
                    onOpenNumbers = { navController.navigate(WearDestination.Numbers.route) },
                    onOpenResult = { navController.navigate(WearDestination.Result.route) },
                    onOpenSettings = { navController.navigate(WearDestination.Settings.route) },
                    onOpenQrOnPhone = {
                        handoffLauncher.openRoute(APP_ROUTE_QR_SCAN) { success ->
                            handoffStatusText =
                                if (success) {
                                    "폰에서 QR 스캔 화면을 열었습니다."
                                } else {
                                    "폰 연결을 확인하고 다시 시도해 주세요."
                                }
                        }
                    },
                )
            }
            composable(WearDestination.Numbers.route) {
                WearNumbersScreen(
                    games = WearPreviewData.games,
                    onBackHome = { navController.popBackStack() },
                )
            }
            composable(WearDestination.Result.route) {
                WearResultScreen(
                    draw = WearPreviewData.draw,
                    summaries = WearPreviewData.resultSummaries,
                    onBackHome = { navController.popBackStack() },
                    onOpenResultOnPhone = {
                        handoffLauncher.openRoute(APP_ROUTE_RESULT) { success ->
                            handoffStatusText =
                                if (success) {
                                    "폰에서 결과 확인 화면으로 이동해 주세요."
                                } else {
                                    "폰 연결을 확인하고 다시 시도해 주세요."
                                }
                        }
                    },
                )
            }
            composable(WearDestination.Settings.route) {
                WearSettingsScreen(
                    purchaseReminderEnabled = purchaseReminderEnabled,
                    resultReminderEnabled = resultReminderEnabled,
                    hapticEnabled = hapticEnabled,
                    statusText = handoffStatusText,
                    onPurchaseReminderChanged = { purchaseReminderEnabled = it },
                    onResultReminderChanged = { resultReminderEnabled = it },
                    onHapticChanged = { hapticEnabled = it },
                    onOpenPhoneSettings = {
                        handoffLauncher.openRoute(APP_ROUTE_SETTINGS) { success ->
                            handoffStatusText =
                                if (success) {
                                    "폰에서 설정 화면을 열었습니다."
                                } else {
                                    "폰 연결을 확인하고 다시 시도해 주세요."
                                }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun WearHomeScreen(
    statusText: String?,
    onOpenNumbers: () -> Unit,
    onOpenResult: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQrOnPhone: () -> Unit,
) {
    WearScreenScaffold { state ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            item {
                Card(
                    onClick = onOpenResult,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "이번 회차 ${WearPreviewData.round}회",
                            style = MaterialTheme.typography.title3,
                        )
                        Text(
                            text = "${WearPreviewData.dDay} • ${WearPreviewData.drawTimeLabel}",
                            style = MaterialTheme.typography.body2,
                        )
                        Text(
                            text = "최근 결과 ${WearPreviewData.draw.numbers.joinToString(" ")} + ${WearPreviewData.draw.bonus}",
                            style = MaterialTheme.typography.caption2,
                        )
                    }
                }
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenNumbers,
                    label = { Text("번호 보기") },
                    secondaryLabel = { Text("A~E 게임 요약") },
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenResult,
                    label = { Text("결과 보기") },
                    secondaryLabel = { Text("당첨번호 + 내 결과") },
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenQrOnPhone,
                    label = { Text("QR 스캔(폰)") },
                    secondaryLabel = { Text("워치에서 폰 앱 실행") },
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenSettings,
                    label = { Text("설정") },
                    secondaryLabel = { Text("알림/진동 관리") },
                )
            }
            statusTextItem(statusText)
        }
    }
}

@Composable
private fun WearNumbersScreen(
    games: List<WearGamePreview>,
    onBackHome: () -> Unit,
) {
    WearScreenScaffold { state ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    text = "이번 주 번호",
                    style = MaterialTheme.typography.title2,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            gameCards(games)
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBackHome,
                    label = { Text("홈으로") },
                    secondaryLabel = { Text("핵심 카드로 복귀") },
                )
            }
        }
    }
}

@Composable
private fun WearResultScreen(
    draw: WearDrawPreview,
    summaries: List<WearResultSummary>,
    onBackHome: () -> Unit,
    onOpenResultOnPhone: () -> Unit,
) {
    val totalPrize = summaries.sumOf { it.prizeAmount }

    WearScreenScaffold { state ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${draw.round}회 당첨",
                            style = MaterialTheme.typography.title3,
                        )
                        LottoBallRow(numbers = draw.numbers, bonus = draw.bonus)
                        Text(
                            text = "${draw.drawDate} 추첨",
                            style = MaterialTheme.typography.caption2,
                        )
                    }
                }
            }
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "예상 당첨금 ${formatWon(totalPrize)}",
                            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                        )
                        summaries.forEach { summary ->
                            Text(
                                text = "${summary.gameLabel} ${summary.rankLabel} · ${formatWon(summary.prizeAmount)}",
                                style = MaterialTheme.typography.body2,
                            )
                        }
                    }
                }
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenResultOnPhone,
                    label = { Text("폰에서 상세 결과") },
                    secondaryLabel = { Text("전체 티켓 분석 열기") },
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBackHome,
                    label = { Text("홈으로") },
                    secondaryLabel = { Text("요약 화면 복귀") },
                )
            }
        }
    }
}

@Composable
private fun WearSettingsScreen(
    purchaseReminderEnabled: Boolean,
    resultReminderEnabled: Boolean,
    hapticEnabled: Boolean,
    statusText: String?,
    onPurchaseReminderChanged: (Boolean) -> Unit,
    onResultReminderChanged: (Boolean) -> Unit,
    onHapticChanged: (Boolean) -> Unit,
    onOpenPhoneSettings: () -> Unit,
) {
    WearScreenScaffold { state ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    text = "워치 설정",
                    style = MaterialTheme.typography.title2,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = purchaseReminderEnabled,
                    onCheckedChange = onPurchaseReminderChanged,
                    label = { Text("구매 알림") },
                    secondaryLabel = { Text("토요일 15:00 알림") },
                    toggleControl = { Switch(checked = purchaseReminderEnabled) },
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = resultReminderEnabled,
                    onCheckedChange = onResultReminderChanged,
                    label = { Text("결과 알림") },
                    secondaryLabel = { Text("토요일 21:00 알림") },
                    toggleControl = { Switch(checked = resultReminderEnabled) },
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = hapticEnabled,
                    onCheckedChange = onHapticChanged,
                    label = { Text("진동 피드백") },
                    secondaryLabel = { Text("버튼/결과 피드백") },
                    toggleControl = { Switch(checked = hapticEnabled) },
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenPhoneSettings,
                    label = { Text("폰 설정 열기") },
                    secondaryLabel = { Text("상세 설정은 폰에서") },
                )
            }
            statusTextItem(statusText)
        }
    }
}

@Composable
private fun WearScreenScaffold(
    content: @Composable (state: ScalingLazyListState) -> Unit,
) {
    val listState = rememberScalingLazyListState()
    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
        content(listState)
    }
}

private fun ScalingLazyListScope.gameCards(games: List<WearGamePreview>) {
    games.forEach { game ->
        item {
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = game.label,
                        style = MaterialTheme.typography.title3,
                    )
                    LottoBallRow(numbers = game.numbers)
                }
            }
        }
    }
}

private fun ScalingLazyListScope.statusTextItem(statusText: String?) {
    if (statusText == null) return
    item {
        Text(
            text = statusText,
            style = MaterialTheme.typography.caption2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LottoBallRow(
    numbers: List<Int>,
    bonus: Int? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            numbers.take(3).forEach { number ->
                LottoBall(number = number)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            numbers.drop(3).forEach { number ->
                LottoBall(number = number)
            }
            if (bonus != null) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                LottoBall(number = bonus)
            }
        }
    }
}

@Composable
private fun LottoBall(number: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(20.dp)
            .background(color = ballColor(number), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

private fun ballColor(number: Int): Color =
    when (number) {
        in 1..10 -> Color(0xFFF3B83A)
        in 11..20 -> Color(0xFF2F7DD1)
        in 21..30 -> Color(0xFFD23B3B)
        in 31..40 -> Color(0xFF3A8D42)
        else -> Color(0xFF6E6E72)
    }

private fun formatWon(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    return "${formatter.format(amount)}원"
}

@Composable
private fun rememberPhoneHandoffLauncher(): PhoneHandoffLauncher {
    val context = LocalContext.current.applicationContext
    val launcher = remember(context) { RemotePhoneHandoffLauncher(context) }
    DisposableEffect(launcher) {
        onDispose {
            launcher.close()
        }
    }
    return launcher
}

private interface PhoneHandoffLauncher {
    fun openRoute(
        route: String,
        onResult: (Boolean) -> Unit,
    )
}

private class RemotePhoneHandoffLauncher(
    context: Context,
) : PhoneHandoffLauncher {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val helper = RemoteActivityHelper(context, executor)

    override fun openRoute(
        route: String,
        onResult: (Boolean) -> Unit,
    ) {
        val deepLink = Uri.parse("$APP_SCHEME://$route")
        val intent =
            Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(deepLink)
        try {
            val future = helper.startRemoteActivity(intent)
            future.addListener(
                {
                    val success = runCatching { future.get() }.isSuccess
                    mainHandler.post { onResult(success) }
                },
                executor,
            )
        } catch (_: Throwable) {
            onResult(false)
        }
    }

    fun close() {
        executor.shutdown()
    }
}

private enum class WearDestination(
    val route: String,
) {
    Home("home"),
    Numbers("numbers"),
    Result("result"),
    Settings("settings"),
}

private data class WearGamePreview(
    val label: String,
    val numbers: List<Int>,
)

private data class WearDrawPreview(
    val round: Int,
    val numbers: List<Int>,
    val bonus: Int,
    val drawDate: String,
)

private data class WearResultSummary(
    val gameLabel: String,
    val rankLabel: String,
    val prizeAmount: Int,
)

private object WearPreviewData {
    val round = 1213
    val dDay = "D-2"
    val drawTimeLabel = "토 20:45"

    val games =
        listOf(
            WearGamePreview("A 게임", listOf(5, 8, 11, 30, 32, 37)),
            WearGamePreview("B 게임", listOf(1, 4, 13, 25, 33, 44)),
            WearGamePreview("C 게임", listOf(2, 9, 14, 28, 39, 41)),
            WearGamePreview("D 게임", listOf(7, 10, 23, 31, 35, 42)),
            WearGamePreview("E 게임", listOf(6, 12, 19, 26, 34, 40)),
        )

    val draw =
        WearDrawPreview(
            round = 1212,
            numbers = listOf(5, 8, 25, 31, 41, 44),
            bonus = 45,
            drawDate = "2026-02-21",
        )

    val resultSummaries =
        listOf(
            WearResultSummary(gameLabel = "A", rankLabel = "5등", prizeAmount = 5_000),
            WearResultSummary(gameLabel = "C", rankLabel = "낙첨", prizeAmount = 0),
            WearResultSummary(gameLabel = "D", rankLabel = "낙첨", prizeAmount = 0),
        )
}
