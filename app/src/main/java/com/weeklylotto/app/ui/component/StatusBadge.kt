package com.weeklylotto.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LottoColors

enum class BadgeTone {
    Neutral,
    Success,
    Danger,
    Accent,
}

@Composable
fun StatusBadge(
    label: String,
    tone: BadgeTone = BadgeTone.Neutral,
) {
    val containerColor =
        when (tone) {
            BadgeTone.Neutral -> LottoColors.Border
            BadgeTone.Success -> LottoColors.SuccessBg
            BadgeTone.Danger -> LottoColors.DangerBg
            BadgeTone.Accent -> LottoColors.Accent.copy(alpha = 0.2f)
        }

    val textColor =
        when (tone) {
            BadgeTone.Neutral -> LottoColors.TextSecondary
            BadgeTone.Success -> LottoColors.SuccessText
            BadgeTone.Danger -> LottoColors.DangerText
            BadgeTone.Accent -> LottoColors.AccentDark
        }

    Box(
        modifier =
            Modifier
                .background(color = containerColor, shape = RoundedCornerShape(12.dp))
                .height(24.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
