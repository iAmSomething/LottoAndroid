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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
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
                        "${game.slot}  $numberSummary"
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
    val hiddenCount = (gameSummaries.size - 3).coerceAtLeast(0)

    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(color = Color(0xFF5B21B6)))
                .cornerRadius(18.dp)
                .padding(12.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "매주로또",
                    style =
                        TextStyle(
                            color = ColorProvider(color = Color.White),
                            fontWeight = FontWeight.Bold,
                        ),
                )
                Text(
                    text = roundLabel,
                    modifier = GlanceModifier.padding(top = 2.dp),
                    style = TextStyle(color = ColorProvider(color = Color(0xFFE9D5FF))),
                )
            }
        }
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = "QR 스캔",
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
                        .background(ColorProvider(color = Color.White))
                        .cornerRadius(10.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                style =
                    TextStyle(
                        color = ColorProvider(color = Color(0xFF5B21B6)),
                        fontWeight = FontWeight.Medium,
                    ),
            )
        }

        if (gameSummaries.isEmpty()) {
            Text(
                text = "이번 주 저장 번호가 없습니다.",
                modifier = GlanceModifier.padding(top = 10.dp),
                style = TextStyle(color = ColorProvider(color = Color(0xFFE9D5FF))),
            )
        } else {
            gameSummaries.take(3).forEach { summary ->
                Row(
                    modifier =
                        GlanceModifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(ColorProvider(color = Color(0x33FFFFFF)))
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = summary,
                        style = TextStyle(color = ColorProvider(color = Color.White)),
                    )
                }
            }
            if (hiddenCount > 0) {
                Text(
                    text = "+ ${hiddenCount}게임 더 있음",
                    modifier = GlanceModifier.padding(top = 6.dp),
                    style = TextStyle(color = ColorProvider(color = Color(0xFFE9D5FF))),
                )
            }
        }
    }
}
