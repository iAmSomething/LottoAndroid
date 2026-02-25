package com.weeklylotto.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = LottoColors.Primary,
        onPrimary = LottoColors.Surface,
        secondary = LottoColors.Accent,
        onSecondary = LottoColors.TextPrimary,
        background = LottoColors.Background,
        onBackground = LottoColors.TextPrimary,
        surface = LottoColors.Surface,
        onSurface = LottoColors.TextPrimary,
        outline = LottoColors.Border,
        error = LottoColors.DangerText,
    )

private val DarkColors =
    darkColorScheme(
        primary = LottoColors.Primary,
        secondary = LottoColors.Accent,
    )

@Composable
fun WeeklyLottoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
