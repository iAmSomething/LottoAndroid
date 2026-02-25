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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
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
            viewModel.clearMessage()
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
                    Button(
                        onClick = {
                            viewModel.cancelPendingSave()
                            restartScanner()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("취소")
                    }
                    Button(
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

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "QR 스캔",
            rightActionText = "닫기",
            onRightClick = onBack,
        )

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
                Text(
                    "세션 저장 수: ${uiState.savedTicketCount}" +
                        (uiState.lastSavedRound?.let { " · 마지막 ${it}회" } ?: ""),
                    color = Color(0xFF424242),
                    fontSize = 12.sp,
                )

                if (hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        CameraScannerPreview(
                            modifier = Modifier.fillMaxSize(),
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
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("카메라 권한 요청")
                    }
                }

                if (!scannerEnabled) {
                    Button(
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
                                Button(onClick = { restartScanner() }) {
                                    Text("재시도")
                                }
                                Button(onClick = { rawInput = "" }) {
                                    Text("수동입력 준비")
                                }
                            }
                        }
                    }
                }

                Button(
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
                    onValueChange = { rawInput = it },
                    label = { Text("QR URL") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { viewModel.parseForConfirm(rawInput) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("파싱")
                }
            }
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
