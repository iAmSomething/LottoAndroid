package com.weeklylotto.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LottoColors {
    val Primary = Color(0xFF7C3AED)
    val PrimaryDark = Color(0xFF6D28D9)
    val Accent = Color(0xFFFB7185)
    val AccentDark = Color(0xFFE11D48)

    val Background = Color(0xFFFFF7FB)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFE9D5FF)

    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF374151)
    val TextMuted = Color(0xFF6B7280)

    val SuccessBg = Color(0xFFDCFCE7)
    val SuccessText = Color(0xFF166534)
    val DangerBg = Color(0xFFFEE2E2)
    val DangerText = Color(0xFF991B1B)

    val Dim = Color(0x59111827)
    val TopBarGradient =
        Brush.horizontalGradient(
            colors = listOf(Primary, PrimaryDark),
        )
}

val LottoGreen = LottoColors.Primary
val LottoYellow = LottoColors.Accent
val LottoBackground = LottoColors.Background
val LottoSurface = LottoColors.Surface
val LottoTextPrimary = LottoColors.TextPrimary
val LottoTextSecondary = LottoColors.TextSecondary
