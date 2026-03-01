package com.weeklylotto.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weeklylotto.app.di.AppGraph
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class PurchaseReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        AppGraph.init(applicationContext)
        val hasCurrentRoundTickets = AppGraph.ticketRepository.observeCurrentRoundTickets().first().isNotEmpty()
        val reminderMessage =
            resolvePurchaseReminderMessage(
                now = LocalDateTime.now(),
                hasCurrentRoundTickets = hasCurrentRoundTickets,
            )

        if (!reminderMessage.shouldNotify) {
            return Result.success()
        }

        ReminderNotificationHelper.notify(
            context = applicationContext,
            id = 1001,
            title = reminderMessage.title,
            body = reminderMessage.body,
            target = ReminderNotificationTarget.PURCHASE,
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "purchase_reminder_work"
    }
}
