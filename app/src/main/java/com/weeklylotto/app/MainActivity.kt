package com.weeklylotto.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.feature.splash.SplashGate
import com.weeklylotto.app.ui.navigation.WeeklyLottoApp
import com.weeklylotto.app.ui.navigation.deepLinkToRoute
import com.weeklylotto.app.ui.theme.LocalMotionSettings
import com.weeklylotto.app.ui.theme.MotionSettings
import com.weeklylotto.app.ui.theme.WeeklyLottoTheme

class MainActivity : ComponentActivity() {
    private var deepLinkUri: Uri? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(applicationContext)
        deepLinkUri = intent?.data
        setContent {
            val reduceMotionEnabled by AppGraph.motionPreferenceStore.observeReduceMotionEnabled().collectAsState(
                initial = false,
            )
            CompositionLocalProvider(
                LocalMotionSettings provides MotionSettings(reduceMotionEnabled = reduceMotionEnabled),
            ) {
                WeeklyLottoTheme {
                    SplashGate {
                        WeeklyLottoApp(initialRoute = deepLinkToRoute(deepLinkUri))
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkUri = intent.data
    }
}
