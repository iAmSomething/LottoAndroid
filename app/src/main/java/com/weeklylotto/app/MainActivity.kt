package com.weeklylotto.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.navigation.WeeklyLottoApp
import com.weeklylotto.app.ui.navigation.deepLinkToRoute
import com.weeklylotto.app.ui.theme.WeeklyLottoTheme

class MainActivity : ComponentActivity() {
    private var deepLinkUri: Uri? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(applicationContext)
        deepLinkUri = intent?.data
        setContent {
            WeeklyLottoTheme {
                WeeklyLottoApp(initialRoute = deepLinkToRoute(deepLinkUri))
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkUri = intent.data
    }
}
