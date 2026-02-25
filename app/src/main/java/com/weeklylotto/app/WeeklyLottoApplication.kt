package com.weeklylotto.app

import android.app.Application
import com.weeklylotto.app.di.AppGraph

class WeeklyLottoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
