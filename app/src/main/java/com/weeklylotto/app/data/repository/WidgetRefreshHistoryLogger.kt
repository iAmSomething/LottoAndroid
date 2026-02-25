package com.weeklylotto.app.data.repository

import android.content.Context
import android.util.Log
import com.weeklylotto.app.BuildConfig
import java.io.File
import java.time.Instant

private const val TAG = "WidgetRefresh"
private const val HISTORY_FILE_NAME = "widget_refresh_history.log"
private const val MAX_HISTORY_LINES = 200

class WidgetRefreshHistoryLogger(
    context: Context,
    private val nowProvider: () -> Instant = Instant::now,
) {
    private val historyFile = File(context.filesDir, HISTORY_FILE_NAME)

    fun log(event: String) {
        if (!BuildConfig.DEBUG) return
        val line = "${nowProvider()} | $event"
        Log.d(TAG, line)
        runCatching {
            val existing = if (historyFile.exists()) historyFile.readLines() else emptyList()
            val updated = trimToMaxLines(existing + line, MAX_HISTORY_LINES)
            historyFile.writeText(updated.joinToString(separator = "\n", postfix = "\n"))
        }.onFailure { error ->
            Log.w(TAG, "Failed to persist widget refresh history: ${error.message}")
        }
    }
}

internal fun trimToMaxLines(
    lines: List<String>,
    maxLines: Int,
): List<String> {
    if (maxLines <= 0) return emptyList()
    return if (lines.size <= maxLines) lines else lines.takeLast(maxLines)
}
