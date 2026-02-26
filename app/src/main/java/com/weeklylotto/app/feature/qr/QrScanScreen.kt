package com.weeklylotto.app.feature.qr

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.MotionButton
import com.weeklylotto.app.ui.component.MotionTextButton
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens

@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel =
        viewModel<QrScanViewModel>(
            factory =
                SingleViewModelFactory {
                    QrScanViewModel(
                        parser = AppGraph.qrTicketParser,
                        ticketRepository = AppGraph.ticketRepository,
                    )
                },
        )

    val uiState by viewModel.uiState.collectAsState()
    var rawInput by remember { mutableStateOf("") }
    var lastHandledQr by remember { mutableStateOf("") }
    var lastHandledAt by remember { mutableLongStateOf(0L) }
    var scannerEnabled by remember { mutableStateOf(true) }
    var torchEnabled by remember { mutableStateOf(false) }
    var torchAvailable by remember { mutableStateOf(false) }
    var isEnvironmentGuideOpen by remember { mutableStateOf(false) }

    fun restartScanner() {
        scannerEnabled = true
        lastHandledQr = ""
        lastHandledAt = 0L
        viewModel.clearFailureGuide()
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCameraPermission = granted
                if (!granted) {
                    torchEnabled = false
                    torchAvailable = false
                }
            },
        )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.latestMessage) {
        uiState.latestMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.savedTicketCount, uiState.continuousScanEnabled, uiState.pendingScan) {
        if (uiState.pendingScan != null) {
            scannerEnabled = false
        } else if (uiState.continuousScanEnabled) {
            scannerEnabled = true
        } else if (uiState.savedTicketCount > 0) {
            scannerEnabled = false
        }
    }

    if (uiState.pendingScan != null) {
        val pending = uiState.pendingScan
        ModalBottomSheet(
            onDismissRequest = { viewModel.cancelPendingSave() },
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("등록할까요?", fontWeight = FontWeight.Bold)
                Text("${pending?.round}회 · ${pending?.games?.size ?: 0}게임")
                val previewNumbers = pending?.games?.firstOrNull()?.numbers.orEmpty()
                if (previewNumbers.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        previewNumbers.forEach { number ->
                            BallChip(
                                number = number.value,
                                state = BallState.Selected,
                                size = 26.dp,
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MotionButton(
                        onClick = {
                            viewModel.cancelPendingSave()
                            restartScanner()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("취소")
                    }
                    MotionButton(
                        onClick = {
                            viewModel.confirmPendingSave()
                            if (uiState.continuousScanEnabled) {
                                restartScanner()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
    if (isEnvironmentGuideOpen) {
        val quickTips = environmentTips(uiState.consecutiveFailureCount, torchAvailable, torchEnabled)
        ModalBottomSheet(
            onDismissRequest = { isEnvironmentGuideOpen = false },
            containerColor = LottoColors.Surface,
            shape = RoundedCornerShape(topStart = LottoDimens.SheetRadius, topEnd = LottoDimens.SheetRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("저조도/반사 환경 가이드", fontWeight = FontWeight.Bold)
                quickTips.forEach { tip ->
                    Text("- $tip", color = LottoColors.TextSecondary, fontSize = 13.sp)
                }
                MotionButton(
                    onClick = { isEnvironmentGuideOpen = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("닫기")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "QR 스캔",
            rightActionText = "닫기",
            onRightClick = onBack,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(LottoDimens.CardRadius),
                border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("실시간 카메라 스캔", fontWeight = FontWeight.Bold)
                    Text(
                        "용지 우측 상단 QR을 프레임 중앙에 맞추면 인식 후 저장 확인 시트가 열립니다.",
                        color = Color(0xFF757575),
                    )
                    Text(
                        "여러 장이 보이면 한 장씩 가까이 촬영하세요.",
                        color = Color(0xFF757575),
                        fontSize = 12.sp,
                    )
                    val modeDescription =
                        if (uiState.continuousScanEnabled) {
                            "여러 장을 연속으로 저장합니다."
                        } else {
                            "1장 저장 후 스캔을 멈춥니다."
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("연속 스캔 모드", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                modeDescription,
                                color = Color(0xFF757575),
                                fontSize = 12.sp,
                            )
                        }
                        Switch(
                            checked = uiState.continuousScanEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.setContinuousScan(enabled)
                                if (enabled) {
                                    restartScanner()
                                }
                            },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("저조도 보정(플래시)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                when {
                                    !hasCameraPermission -> "카메라 권한 허용 후 사용 가능합니다."
                                    !torchAvailable -> "현재 기기/에뮬레이터는 플래시를 지원하지 않습니다."
                                    torchEnabled -> "플래시가 켜져 있어 저조도 인식이 개선됩니다."
                                    else -> "어두우면 켜고, 반사가 강하면 끈 뒤 각도를 바꿔보세요."
                                },
                                color = Color(0xFF757575),
                                fontSize = 12.sp,
                            )
                        }
                        Switch(
                            checked = torchEnabled && torchAvailable,
                            onCheckedChange = { enabled ->
                                if (torchAvailable) {
                                    torchEnabled = enabled
                                }
                            },
                            enabled = hasCameraPermission && torchAvailable,
                        )
                    }
                    MotionTextButton(
                        onClick = { isEnvironmentGuideOpen = true },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("환경 가이드 보기")
                    }
                    Text(
                        "세션 저장 수: ${uiState.savedTicketCount}" +
                            (uiState.lastSavedRound?.let { " · 마지막 ${it}회" } ?: ""),
                        color = Color(0xFF424242),
                        fontSize = 12.sp,
                    )
                    uiState.latestMessage?.let { message ->
                        Text(
                            text = message,
                            color = Color(0xFF616161),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (hasCameraPermission) {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                            CameraScannerPreview(
                                modifier = Modifier.fillMaxSize(),
                                torchEnabled = torchEnabled,
                                onTorchAvailabilityChanged = { supported ->
                                    torchAvailable = supported
                                    if (!supported) {
                                        torchEnabled = false
                                    }
                                },
                                onQrDetected = { qrValue ->
                                    if (!scannerEnabled) return@CameraScannerPreview
                                    val now = System.currentTimeMillis()
                                    if (qrValue == lastHandledQr && now - lastHandledAt < 5_000L) {
                                        return@CameraScannerPreview
                                    }
                                    lastHandledQr = qrValue
                                    lastHandledAt = now
                                    rawInput = qrValue
                                    viewModel.parseForConfirm(qrValue)
                                },
                            )
                            QrGuideOverlay(modifier = Modifier.fillMaxSize())
                        }
                    } else {
                        Text("카메라 권한이 필요합니다.", color = Color(0xFFD32F2F))
                        MotionButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("카메라 권한 요청")
                        }
                    }

                    if (!scannerEnabled) {
                        MotionButton(
                            onClick = { restartScanner() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("다음 티켓 스캔")
                        }
                    }

                    if (uiState.consecutiveFailureCount > 0) {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF3E0)),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "스캔 실패 ${uiState.consecutiveFailureCount}회",
                                    color = Color(0xFFEF6C00),
                                    fontWeight = FontWeight.Bold,
                                )
                                uiState.failureGuideMessage?.let { guide ->
                                    Text(
                                        text = guide,
                                        color = Color(0xFF5D4037),
                                        fontSize = 12.sp,
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MotionButton(onClick = { restartScanner() }) {
                                        Text("재시도")
                                    }
                                    if (uiState.shouldRecommendTorch && torchAvailable && !torchEnabled) {
                                        MotionButton(onClick = { torchEnabled = true }) {
                                            Text("플래시 켜기")
                                        }
                                    }
                                    MotionButton(onClick = { rawInput = "" }) {
                                        Text("수동입력 준비")
                                    }
                                }
                                if (uiState.shouldRecommendManualInput) {
                                    Text(
                                        text = "실패가 반복되면 하단 수동 입력 백업으로 먼저 등록하세요.",
                                        color = Color(0xFF5D4037),
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }

                    MotionButton(
                        onClick = { viewModel.resetSessionCount() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("세션 카운터 초기화")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(LottoDimens.CardRadius),
                border = androidx.compose.foundation.BorderStroke(1.dp, LottoColors.Border),
                colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("수동 입력 백업", fontWeight = FontWeight.Bold)
                    Text("스캔이 어려운 경우 URL을 붙여넣어 등록하세요.", color = Color(0xFF757575))
                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = {
                            rawInput = it
                            if (it.isNotBlank()) {
                                scannerEnabled = false
                            }
                        },
                        label = { Text("QR URL") },
                        modifier = Modifier.fillMaxWidth().testTag("qr_manual_input"),
                    )
                    MotionButton(
                        onClick = {
                            scannerEnabled = false
                            viewModel.parseForConfirm(rawInput)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("qr_manual_parse"),
                    ) {
                        Text("파싱")
                    }
                }
            }
            Box(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QrGuideOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.45f), shape = RectangleShape),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.58f)
                    .height(128.dp)
                    .border(width = 2.dp, color = Color(0xFFFFC107), shape = RectangleShape),
        )
        Text(
            text = "QR을 프레임 중앙에 맞춰주세요",
            color = Color.White,
            fontSize = 12.sp,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 8.dp)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Text(
            text = "용지 1장씩 스캔 권장",
            color = Color.White,
            fontSize = 11.sp,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-8).dp)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(34.dp)
                    .border(width = 1.5.dp, color = Color(0xFFB2DFDB), shape = RectangleShape),
        )
        Text(
            text = "우측 상단 QR 위치",
            color = Color(0xFFB2DFDB),
            fontSize = 10.sp,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 50.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .background(Color.Black.copy(alpha = 0.35f)),
        )
    }
}

private fun environmentTips(
    failureCount: Int,
    torchAvailable: Boolean,
    torchEnabled: Boolean,
): List<String> {
    val baseTips =
        mutableListOf(
            "QR이 있는 용지 우측 상단이 프레임 중앙 박스 안에 오도록 맞춰주세요.",
            "형광등 반사가 있으면 용지를 10~15도 기울여 반짝임을 피하세요.",
            "촬영 거리는 15~20cm를 유지하고, 한 프레임에는 한 장만 넣어주세요.",
        )

    if (torchAvailable) {
        baseTips +=
            if (torchEnabled) {
                "플래시가 켜져 있습니다. 과노출이면 플래시를 끄고 밝은 방향으로 이동하세요."
            } else {
                "저조도면 플래시를 켜서 QR 대비를 높이세요."
            }
    }
    if (failureCount >= 2) {
        baseTips += "실패가 2회 이상이면 카메라를 잠시 멈추고 각도/거리부터 다시 맞추세요."
    }
    if (failureCount >= 4) {
        baseTips += "반복 실패 시 하단 수동 입력 백업으로 URL 등록을 진행하세요."
    }
    return baseTips
}
