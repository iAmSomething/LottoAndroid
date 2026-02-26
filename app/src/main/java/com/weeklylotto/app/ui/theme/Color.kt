package com.weeklylotto.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LottoColors {
    val Primary = Color(0xFF0F5B63)
    val PrimaryDark = Color(0xFF0A3F45)
    val Accent = Color(0xFFE0B34A)
    val AccentDark = Color(0xFFB98E2E)

    val Background = Color(0xFFF7F4EE)
    val Surface = Color(0xFFFFFCF7)
    val Border = Color(0xFFDDE3E8)

    val TextPrimary = Color(0xFF1C2228)
    val TextSecondary = Color(0xFF4A5561)
    val TextMuted = Color(0xFF55606B)

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
