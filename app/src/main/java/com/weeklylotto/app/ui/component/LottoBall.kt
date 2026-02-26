package com.weeklylotto.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LottoTypeTokens

enum class BallState {
    Normal,
    Selected,
    Locked,
    Hit,
    Bonus,
    Muted,
}

@Composable
@Suppress("CyclomaticComplexMethod")
fun BallChip(
    number: Int?,
    state: BallState = BallState.Normal,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    val value = number ?: 0
    val baseColor =
        when (value) {
            in 1..10 -> Color(0xFFFBC02D)
            in 11..20 -> Color(0xFF1976D2)
            in 21..30 -> Color(0xFFD32F2F)
            in 31..40 -> Color(0xFF388E3C)
            in 41..45 -> Color(0xFF616161)
            else -> Color(0xFFE5E7EB)
        }

    val background =
        when (state) {
            BallState.Normal -> if (number == null) Color.Transparent else baseColor
            BallState.Selected -> baseColor
            BallState.Locked -> baseColor
            BallState.Hit -> baseColor
            BallState.Bonus -> baseColor
            BallState.Muted -> Color.Transparent
        }

    val borderColor =
        when (state) {
            BallState.Bonus -> Color(0xFFFFD54F)
            BallState.Hit -> Color.White
            BallState.Locked -> Color(0xFF111827)
            BallState.Muted -> Color(0xFFBDBDBD)
            else -> if (number == null) Color(0xFFD1D5DB) else Color.Transparent
        }

    val textColor =
        when {
            number == null -> Color(0xFF9CA3AF)
            state == BallState.Muted -> Color(0xFFBDBDBD)
            else -> Color.White
        }

    Box(
        modifier =
            modifier
                .semantics(mergeDescendants = true) {
                    contentDescription = buildBallChipAccessibilityLabel(number, state)
                }
                .size(size)
                .background(background, CircleShape)
                .border(width = if (state == BallState.Bonus) 2.dp else 1.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number?.toString()?.padStart(2, '0') ?: "-",
            color = textColor,
            style = LottoTypeTokens.NumericBall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun LottoBall(
    number: Int,
    highlighted: Boolean = true,
    modifier: Modifier = Modifier,
) {
    BallChip(
        number = number,
        state = if (highlighted) BallState.Normal else BallState.Muted,
        size = 28.dp,
        modifier = modifier.alpha(if (highlighted) 1f else 0.9f),
    )
}

internal fun buildBallChipAccessibilityLabel(
    number: Int?,
    state: BallState,
): String {
    if (number == null) return "미입력 번호"

    val formattedNumber = number.toString().padStart(2, '0')
    return when (state) {
        BallState.Bonus -> "보너스 번호 $formattedNumber"
        BallState.Locked -> "번호 $formattedNumber 고정됨"
        BallState.Hit -> "번호 $formattedNumber 당첨 일치"
        BallState.Muted -> "번호 $formattedNumber 비강조"
        BallState.Normal,
        BallState.Selected,
        -> "번호 $formattedNumber"
    }
}
