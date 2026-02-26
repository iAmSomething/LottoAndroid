package com.weeklylotto.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class MotionSettings(
    val reduceMotionEnabled: Boolean = false,
) {
    fun durationMillis(
        defaultMillis: Int,
        minMillis: Int = 60,
    ): Int {
        if (!reduceMotionEnabled) return defaultMillis
        return (defaultMillis / 2).coerceAtLeast(minMillis)
    }
}

val LocalMotionSettings = staticCompositionLocalOf { MotionSettings() }
