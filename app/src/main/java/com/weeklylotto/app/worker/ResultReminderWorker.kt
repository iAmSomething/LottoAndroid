package com.weeklylotto.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.error.AppResult
import kotlinx.coroutines.flow.first

class ResultReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        AppGraph.init(applicationContext)
        val latestRound = when (val result = AppGraph.drawRepository.fetchLatest()) {
            is AppResult.Success -> result.value.round
            is AppResult.Failure -> null
        }
        val hasTicketsForLatestRound =
            latestRound?.let { round ->
                AppGraph.ticketRepository.observeTicketsByRound(round).first().isNotEmpty()
            } ?: false
        val lastViewedRound = AppGraph.resultViewTracker.loadLastViewedRound()
        val reminderMessage =
            resolveResultReminderMessage(
                latestRoundNumber = latestRound?.number,
                hasTicketsForLatestRound = hasTicketsForLatestRound,
                lastViewedRound = lastViewedRound,
            )

        if (!reminderMessage.shouldNotify) {
            return Result.success()
        }

        ReminderNotificationHelper.notify(
            context = applicationContext,
            id = 1002,
            title = reminderMessage.title,
            body = reminderMessage.body,
            target = ReminderNotificationTarget.RESULT,
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "result_reminder_work"
    }
}
