package com.weeklylotto.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.weeklylotto.app.R

object ReminderNotificationHelper {
    private const val CHANNEL_ID = "weekly_lotto_reminders"

    fun notify(
        context: Context,
        id: Int,
        title: String,
        body: String,
    ) {
        ensureChannel(context)
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "매주로또 알림",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }
}
