package com.weeklylotto.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LottoColors

enum class LottoGlyphTone {
    Primary,
    Accent,
    Neutral,
}

@Composable
fun LottoGlyphIcon(
    tone: LottoGlyphTone,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        when (tone) {
            LottoGlyphTone.Primary -> LottoColors.Primary.copy(alpha = 0.12f)
            LottoGlyphTone.Accent -> LottoColors.Accent.copy(alpha = 0.2f)
            LottoGlyphTone.Neutral -> LottoColors.Border.copy(alpha = 0.4f)
        }
    val contentColor =
        when (tone) {
            LottoGlyphTone.Primary -> LottoColors.PrimaryDark
            LottoGlyphTone.Accent -> LottoColors.AccentDark
            LottoGlyphTone.Neutral -> LottoColors.TextSecondary
        }

    Box(
        modifier =
            modifier
                .size(24.dp)
                .background(containerColor, RoundedCornerShape(7.dp))
                .border(width = 1.dp, color = LottoColors.Border, shape = RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(contentColor, CircleShape),
        )
    }
}

@Composable
fun LottoSectionLabel(
    text: String,
    tone: LottoGlyphTone,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LottoGlyphIcon(tone = tone)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = LottoColors.TextMuted,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun LottoLayeredCardBackground(
    modifier: Modifier = Modifier,
    accent: Color = LottoColors.Primary.copy(alpha = 0.06f),
) {
    Box(
        modifier =
            modifier
                .background(
                    color = LottoColors.Surface,
                    shape = RoundedCornerShape(18.dp),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        color = accent,
                        shape = RoundedCornerShape(18.dp),
                    ),
        )
    }
}
