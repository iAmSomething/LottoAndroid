package com.weeklylotto.app.data.repository

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.weeklylotto.app.domain.model.ReminderConfig
import com.weeklylotto.app.domain.service.ReminderScheduler
import com.weeklylotto.app.worker.PurchaseReminderWorker
import com.weeklylotto.app.worker.ResultReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime

class WorkManagerReminderScheduler(
    context: Context,
) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context)

    override suspend fun schedule(config: ReminderConfig) =
        withContext(Dispatchers.IO) {
            if (!config.enabled) {
                cancelAll()
                return@withContext
            }

            val purchaseDelay =
                nextDelay(
                    config.purchaseReminderDay,
                    config.purchaseReminderTime.hour,
                    config.purchaseReminderTime.minute,
                )
            val resultDelay =
                nextDelay(config.resultReminderDay, config.resultReminderTime.hour, config.resultReminderTime.minute)

            val purchaseRequest =
                PeriodicWorkRequestBuilder<PurchaseReminderWorker>(java.time.Duration.ofDays(7))
                    .setInitialDelay(purchaseDelay)
                    .build()

            val resultRequest =
                PeriodicWorkRequestBuilder<ResultReminderWorker>(java.time.Duration.ofDays(7))
                    .setInitialDelay(resultDelay)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                PurchaseReminderWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                purchaseRequest,
            )
            workManager.enqueueUniquePeriodicWork(
                ResultReminderWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                resultRequest,
            )
        }

    override suspend fun cancelAll() =
        withContext(Dispatchers.IO) {
            workManager.cancelUniqueWork(PurchaseReminderWorker.UNIQUE_NAME)
            workManager.cancelUniqueWork(ResultReminderWorker.UNIQUE_NAME)
            Unit
        }

    private fun nextDelay(
        dayOfWeek: DayOfWeek,
        hour: Int,
        minute: Int,
    ): Duration {
        val now = LocalDateTime.now()
        var candidate = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        while (candidate.dayOfWeek != dayOfWeek || !candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }

        return Duration.between(now, candidate)
    }
}
