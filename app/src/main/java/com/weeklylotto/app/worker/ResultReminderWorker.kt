package com.weeklylotto.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ResultReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        ReminderNotificationHelper.notify(
            context = applicationContext,
            id = 1002,
            title = "이번 주 로또 결과 확인",
            body = "저장한 번호의 당첨 결과를 확인해보세요.",
            target = ReminderNotificationTarget.RESULT,
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "result_reminder_work"
    }
}
