package com.weeklylotto.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.weeklylotto.app.R
import com.weeklylotto.app.MainActivity
import com.weeklylotto.app.ui.navigation.routeToDeepLink

object ReminderNotificationHelper {
    private const val CHANNEL_ID = "weekly_lotto_reminders"

    fun notify(
        context: Context,
        id: Int,
        title: String,
        body: String,
        target: ReminderNotificationTarget,
    ) {
        ensureChannel(context)
        val openAppIntent = createOpenAppIntent(context, target, id)
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(openAppIntent)
                .setAutoCancel(true)
                .addAction(
                    Action.Builder(
                        0,
                        context.getString(R.string.notification_action_open_app),
                        openAppIntent,
                    ).build(),
                )
                .addAction(
                    Action.Builder(
                        0,
                        context.getString(R.string.notification_action_remind_later),
                        createSnoozeIntent(context, target, id),
                    ).build(),
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun createOpenAppIntent(
        context: Context,
        target: ReminderNotificationTarget,
        id: Int,
    ) = PendingIntent.getActivity(
        context,
        id * 10 + 1,
        Intent(context, MainActivity::class.java).apply {
            data = routeToDeepLink(target.route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun createSnoozeIntent(
        context: Context,
        target: ReminderNotificationTarget,
        id: Int,
    ) = PendingIntent.getBroadcast(
        context,
        id * 10 + 2,
        Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_REMIND_LATER
            putExtra(ReminderActionReceiver.EXTRA_TARGET_KEY, target.key)
            putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, id)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

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
