package com.weeklylotto.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != ACTION_REMIND_LATER) return

        val target = ReminderNotificationTarget.fromKey(intent.getStringExtra(EXTRA_TARGET_KEY)) ?: return
        scheduleSnooze(context, target)

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        if (notificationId != 0) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }

    private fun scheduleSnooze(
        context: Context,
        target: ReminderNotificationTarget,
    ) {
        val request = buildSnoozeRequest(target)
        WorkManager.getInstance(context).enqueueUniqueWork(
            target.snoozeWorkName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun buildSnoozeRequest(target: ReminderNotificationTarget): OneTimeWorkRequest {
        val builder =
            when (target) {
                ReminderNotificationTarget.PURCHASE -> OneTimeWorkRequestBuilder<PurchaseReminderWorker>()
                ReminderNotificationTarget.RESULT -> OneTimeWorkRequestBuilder<ResultReminderWorker>()
            }

        return builder
            .setInitialDelay(Duration.ofMinutes(SNOOZE_MINUTES))
            .build()
    }

    companion object {
        const val ACTION_REMIND_LATER = "com.weeklylotto.app.action.REMIND_LATER"
        const val EXTRA_TARGET_KEY = "extra_target_key"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        private const val SNOOZE_MINUTES = 30L
    }
}
