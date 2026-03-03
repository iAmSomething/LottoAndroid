package com.weeklylotto.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.weeklylotto.app.R

val LottoDisplayFontFamily =
    FontFamily(
        Font(R.font.brand_roboto_condensed_variable, weight = FontWeight.Medium),
        Font(R.font.brand_roboto_condensed_variable, weight = FontWeight.SemiBold),
        Font(R.font.brand_roboto_condensed_variable, weight = FontWeight.Bold),
        Font(R.font.brand_roboto_condensed_variable, weight = FontWeight.Black),
    )

val LottoBodyFontFamily =
    FontFamily(
        Font(R.font.brand_noto_sans_kr_variable, weight = FontWeight.Normal),
        Font(R.font.brand_noto_sans_kr_variable, weight = FontWeight.Medium),
        Font(R.font.brand_noto_sans_kr_variable, weight = FontWeight.SemiBold),
        Font(R.font.brand_noto_sans_kr_variable, weight = FontWeight.Bold),
    )

val LottoNumericFontFamily =
    FontFamily(
        Font(R.font.brand_roboto_mono_variable, weight = FontWeight.Medium),
        Font(R.font.brand_roboto_mono_variable, weight = FontWeight.Bold),
        Font(R.font.brand_roboto_mono_variable, weight = FontWeight.Black),
    )

val Typography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = LottoDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = LottoDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 30.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 24.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = LottoBodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            ),
    )

object LottoTypeTokens {
    val NumericHeadline =
        TextStyle(
            fontFamily = LottoNumericFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 36.sp,
        )
    val NumericTitle =
        TextStyle(
            fontFamily = LottoNumericFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        )
    val NumericBody =
        TextStyle(
            fontFamily = LottoNumericFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    val NumericBall =
        TextStyle(
            fontFamily = LottoNumericFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 13.sp,
        )
}
