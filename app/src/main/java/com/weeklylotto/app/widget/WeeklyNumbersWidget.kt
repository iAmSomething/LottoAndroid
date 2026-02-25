package com.weeklylotto.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.navigation.AppDestination
import com.weeklylotto.app.ui.navigation.routeToDeepLink

class WeeklyNumbersWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val snapshot = AppGraph.widgetDataProvider.loadWeeklyNumbersSnapshot()
        val games = snapshot.bundles.firstOrNull()?.games.orEmpty()

        provideContent {
            WeeklyNumbersWidgetContent(
                roundLabel = snapshot.roundLabel,
                gameSummaries =
                    games.map { game ->
                        val numberSummary = game.numbers.joinToString(" ") { it.value.toString().padStart(2, '0') }
                        "${game.slot} $numberSummary"
                    },
            )
        }
    }
}

class WeeklyNumbersWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyNumbersWidget()
}

@Composable
private fun WeeklyNumbersWidgetContent(
    roundLabel: String,
    gameSummaries: List<String>,
) {
    val hiddenCount = (gameSummaries.size - 2).coerceAtLeast(0)

    Column(
        modifier =
            GlanceModifier
                .background(ColorProvider(color = Color.White))
                .padding(10.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = roundLabel,
                style = TextStyle(color = ColorProvider(color = Color(0xFF00796B))),
            )
        }
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
                text = "QR",
                modifier =
                    GlanceModifier
                        .clickable(
                            actionStartActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    routeToDeepLink(AppDestination.QrScan.route),
                                ),
                            ),
                        )
                        .background(ColorProvider(color = Color(0xFFE0F2F1)))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                style = TextStyle(color = ColorProvider(color = Color(0xFF00796B))),
            )
        }

        if (gameSummaries.isEmpty()) {
            Text(
                text = "저장된 번호가 없습니다.",
                modifier = GlanceModifier.padding(top = 8.dp),
                style = TextStyle(color = ColorProvider(color = Color(0xFF757575))),
            )
        } else {
            gameSummaries.take(2).forEach { summary ->
                Text(
                    text = summary,
                    modifier = GlanceModifier.padding(top = 6.dp),
                    style = TextStyle(color = ColorProvider(color = Color(0xFF212121))),
                )
            }
            if (hiddenCount > 0) {
                Text(
                    text = "+ ${hiddenCount}게임",
                    modifier = GlanceModifier.padding(top = 4.dp),
                    style = TextStyle(color = ColorProvider(color = Color(0xFF757575))),
                )
            }
        }
    }
}
