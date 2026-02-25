package com.weeklylotto.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.weeklylotto.app.di.AppGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching {
                AppGraph.init(context.applicationContext)
                val config = AppGraph.reminderConfigStore.load()
                AppGraph.reminderScheduler.schedule(config)
            }
            pendingResult.finish()
        }
    }
}
