package com.weeklylotto.app.feature.splash

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.service.AnalyticsActionValue
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.ui.theme.LocalMotionSettings
import com.weeklylotto.app.ui.theme.LottoColors
import kotlinx.coroutines.delay

@Composable
fun SplashGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val analyticsLogger = AppGraph.analyticsLogger
    val motionSettings = LocalMotionSettings.current
    val preferences = remember(context) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val seenIntro = preferences.getBoolean(KEY_SEEN_INTRO, false)
        val mode = if (seenIntro) AnalyticsActionValue.WARM else AnalyticsActionValue.COLD
        analyticsLogger.log(
            event = AnalyticsEvent.MOTION_SPLASH_SHOWN,
            params =
                mapOf(
                    AnalyticsParamKey.SCREEN to "splash",
                    AnalyticsParamKey.COMPONENT to "splash_gate",
                    AnalyticsParamKey.ACTION to mode,
                ),
        )

        if (seenIntro) {
            analyticsLogger.log(
                event = AnalyticsEvent.MOTION_SPLASH_SKIP,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "splash",
                        AnalyticsParamKey.COMPONENT to "splash_gate",
                        AnalyticsParamKey.ACTION to AnalyticsActionValue.COMPACT,
                    ),
            )
        } else {
            preferences.edit().putBoolean(KEY_SEEN_INTRO, true).apply()
        }

        val rawDuration = if (seenIntro) WARM_DURATION_MS else COLD_DURATION_MS
        delay(motionSettings.durationMillis(rawDuration.toInt(), minMillis = 80).toLong())
        visible = false
    }

    val enterTransition: EnterTransition =
        if (motionSettings.reduceMotionEnabled) {
            fadeIn(animationSpec = tween(durationMillis = motionSettings.durationMillis(defaultMillis = 180)))
        } else {
            fadeIn(animationSpec = tween(durationMillis = 260)) +
                scaleIn(
                    initialScale = 0.985f,
                    animationSpec = tween(durationMillis = 260),
                )
        }

    val exitTransition: ExitTransition =
        if (motionSettings.reduceMotionEnabled) {
            fadeOut(animationSpec = tween(durationMillis = motionSettings.durationMillis(defaultMillis = 180)))
        } else {
            fadeOut(animationSpec = tween(durationMillis = 220)) +
                scaleOut(
                    targetScale = 1.01f,
                    animationSpec = tween(durationMillis = 220),
                )
        }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        AnimatedVisibility(
            visible = visible,
            enter = enterTransition,
            exit = exitTransition,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(LottoColors.PrimaryDark),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "매주로또",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "정기 구매 · 행운 · 스마트 관리",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

private const val PREF_NAME = "weeklylotto_splash"
private const val KEY_SEEN_INTRO = "seen_intro"
private const val COLD_DURATION_MS = 900L
private const val WARM_DURATION_MS = 300L
