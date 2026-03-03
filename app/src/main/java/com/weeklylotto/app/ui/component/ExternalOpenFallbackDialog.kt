package com.weeklylotto.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LottoColors

@Composable
fun ExternalOpenFallbackDialog(
    url: String,
    onOpenBrowser: () -> Unit,
    onCopyLink: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("외부 이동에 실패했습니다") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "기본 브라우저로 다시 열거나 링크를 복사해 수동으로 접속해 주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LottoColors.TextSecondary,
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = LottoColors.Primary,
                )
            }
        },
        confirmButton = {
            MotionButton(onClick = onOpenBrowser) {
                Text("기본 브라우저로 열기")
            }
        },
        dismissButton = {
            MotionTextButton(onClick = onCopyLink) {
                Text("링크 복사")
            }
        },
    )
}
