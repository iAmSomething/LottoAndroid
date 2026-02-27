package com.weeklylotto.wear.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weeklylotto.wear.MainActivity
import com.weeklylotto.wear.R

class WeeklyLottoComplicationService : SuspendingComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? = buildData(type)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? =
        buildData(request.complicationType)

    private fun buildData(type: ComplicationType): ComplicationData? {
        val tapAction = createLaunchIntent()
        val title = plainText(getString(R.string.complication_title))
        val text = plainText(getString(R.string.complication_text_short))
        val contentDescription = plainText(getString(R.string.complication_content_description))

        return when (type) {
            ComplicationType.SHORT_TEXT ->
                ShortTextComplicationData.Builder(
                    text = text,
                    contentDescription = contentDescription,
                )
                    .setTitle(title)
                    .setTapAction(tapAction)
                    .build()

            ComplicationType.LONG_TEXT ->
                LongTextComplicationData.Builder(
                    text = plainText(getString(R.string.complication_text_long)),
                    contentDescription = contentDescription,
                )
                    .setTitle(title)
                    .setTapAction(tapAction)
                    .build()

            else -> null
        }
    }

    private fun createLaunchIntent(): PendingIntent {
        val launchIntent =
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(this@WeeklyLottoComplicationService, MainActivity::class.java)
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun plainText(value: String): PlainComplicationText =
        PlainComplicationText.Builder(value).build()
}
