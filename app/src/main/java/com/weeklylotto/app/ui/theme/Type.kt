package com.weeklylotto.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography =
    Typography(
        titleLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 18.sp, lineHeight = 24.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 20.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    )
