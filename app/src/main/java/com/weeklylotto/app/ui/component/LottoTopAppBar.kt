package com.weeklylotto.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens

@Composable
fun LottoTopAppBar(
    title: String,
    rightActionText: String? = null,
    onRightClick: (() -> Unit)? = null,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(LottoDimens.TopBarHeight)
                .background(LottoColors.TopBarGradient),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = LottoDimens.ScreenPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            if (rightActionText != null) {
                Text(
                    text = rightActionText,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = onRightClick != null) { onRightClick?.invoke() },
                )
            }
        }
    }
}
