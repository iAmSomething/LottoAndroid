package com.weeklylotto.app.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.component.MotionButton
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel =
        viewModel<SettingsViewModel>(
            factory =
                SingleViewModelFactory {
                    SettingsViewModel(
                        reminderConfigStore = AppGraph.reminderConfigStore,
                        reminderScheduler = AppGraph.reminderScheduler,
                        motionPreferenceStore = AppGraph.motionPreferenceStore,
                    )
                },
        )
    val uiState by viewModel.uiState.collectAsState()
    val requiresNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val hasNotificationPermission =
        !requiresNotificationPermission ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (granted) {
                    viewModel.setEnabled(true)
                } else {
                    viewModel.setEnabled(false)
                    Toast
                        .makeText(
                            context,
                            "알림 권한이 없어 예약 알림을 켤 수 없습니다.",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            },
        )

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(LottoColors.Background)) {
        LottoTopAppBar(
            title = "알림 설정",
            rightActionText = "닫기",
            onRightClick = onNavigateBack,
        )

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("알림 사용")
                    Switch(
                        checked = uiState.config.enabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !hasNotificationPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setEnabled(enabled)
                            }
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text("모션 축소")
                        Text(
                            "애니메이션 시간 50% 축소, 변형 모션 최소화",
                            color = LottoColors.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = uiState.reduceMotionEnabled,
                        onCheckedChange = viewModel::setReduceMotionEnabled,
                    )
                }

                Text(
                    "구매 알림: ${uiState.config.purchaseReminderDay} ${uiState.config.purchaseReminderTime}",
                    color = LottoColors.TextSecondary,
                )
                Text(
                    "결과 알림: ${uiState.config.resultReminderDay} ${uiState.config.resultReminderTime}",
                    color = LottoColors.TextSecondary,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MotionButton(onClick = viewModel::useDefaultSchedule) { Text("기본값") }
                    MotionButton(onClick = viewModel::useFridayEveningSchedule) { Text("금요일 저녁") }
                }

                MotionButton(
                    onClick = {
                        if (uiState.config.enabled && !hasNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.saveSchedule()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("알림 설정 저장")
                }
            }
        }
    }
}
