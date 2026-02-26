package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.ui.theme.LottoColors
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ColorContrastTest {
    @Test
    fun 핵심_텍스트_대비가_wcag_aa_기준을_만족한다() {
        assertThat(contrastRatio(LottoColors.TextPrimary, LottoColors.Surface)).isAtLeast(4.5f)
        assertThat(contrastRatio(LottoColors.TextSecondary, LottoColors.Background)).isAtLeast(4.5f)
        assertThat(contrastRatio(LottoColors.TextMuted, LottoColors.Background)).isAtLeast(4.5f)
    }

    @Test
    fun 상태_배지_텍스트_대비가_wcag_aa_기준을_만족한다() {
        assertThat(contrastRatio(LottoColors.SuccessText, LottoColors.SuccessBg)).isAtLeast(4.5f)
        assertThat(contrastRatio(LottoColors.DangerText, LottoColors.DangerBg)).isAtLeast(4.5f)
    }

    @Test
    fun 상단바_화이트_텍스트_대비가_wcag_aa_기준을_만족한다() {
        assertThat(contrastRatio(LottoColors.Surface, LottoColors.Primary)).isAtLeast(4.5f)
        assertThat(contrastRatio(LottoColors.Surface, LottoColors.PrimaryDark)).isAtLeast(4.5f)
    }
}

private fun contrastRatio(
    foreground: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color,
): Float {
    val fgLuminance = relativeLuminance(foreground)
    val bgLuminance = relativeLuminance(background)
    val lighter = max(fgLuminance, bgLuminance)
    val darker = min(fgLuminance, bgLuminance)
    return ((lighter + 0.05f) / (darker + 0.05f))
}

private fun relativeLuminance(color: androidx.compose.ui.graphics.Color): Float {
    fun toLinear(channel: Float): Float =
        if (channel <= 0.03928f) {
            channel / 12.92f
        } else {
            ((channel + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }

    val r = toLinear(color.red)
    val g = toLinear(color.green)
    val b = toLinear(color.blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
