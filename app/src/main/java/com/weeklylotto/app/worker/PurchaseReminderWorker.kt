package com.weeklylotto.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PurchaseReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        ReminderNotificationHelper.notify(
            context = applicationContext,
            id = 1001,
            title = "이번 주 로또 구매 시간",
            body = "구매하실 번호를 확인하고 QR로 등록해보세요.",
            target = ReminderNotificationTarget.PURCHASE,
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "purchase_reminder_work"
    }
}
