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

class ResultSummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val snapshot = AppGraph.widgetDataProvider.loadResultSummarySnapshot()
        val draw = snapshot.drawResult
        val mainNumbers = draw?.mainNumbers?.joinToString(" ") { it.value.toString().padStart(2, '0') } ?: "--"
        val bonusNumber = draw?.bonus?.value?.toString()?.padStart(2, '0') ?: "--"

        provideContent {
            ResultSummaryWidgetContent(
                roundLabel = snapshot.roundLabel,
                summary = snapshot.summaryText,
                mainNumbers = mainNumbers,
                bonusNumber = bonusNumber,
            )
        }
    }
}

class ResultSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ResultSummaryWidget()
}

@Composable
private fun ResultSummaryWidgetContent(
    roundLabel: String,
    summary: String,
    mainNumbers: String,
    bonusNumber: String,
) {
    val summaryColor =
        if ("당첨" in summary) {
            Color(0xFF7C3AED)
        } else {
            Color(0xFFF59E0B)
        }

    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(color = Color.White))
                .cornerRadius(18.dp)
                .padding(12.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column {
                Text(
                    text = roundLabel,
                    style =
                        TextStyle(
                            color = ColorProvider(color = Color(0xFF111827)),
                            fontWeight = FontWeight.Bold,
                        ),
                )
                Text(
                    text = summary,
                    modifier = GlanceModifier.padding(top = 4.dp),
                    style =
                        TextStyle(
                            color = ColorProvider(color = summaryColor),
                            fontWeight = FontWeight.Medium,
                        ),
                )
            }
        }
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = "결과 보기",
                modifier =
                    GlanceModifier
                        .clickable(
                            actionStartActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    routeToDeepLink(AppDestination.Result.route),
                                ),
                            ),
                        )
                        .background(ColorProvider(color = Color(0xFFF3E8FF)))
                        .cornerRadius(10.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                style =
                    TextStyle(
                        color = ColorProvider(color = Color(0xFF6D28D9)),
                        fontWeight = FontWeight.Medium,
                    ),
            )
        }

        Row(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(ColorProvider(color = Color(0xFFF8FAFC)))
                    .cornerRadius(10.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(
                text = mainNumbers,
                style = TextStyle(color = ColorProvider(color = Color(0xFF1F2937))),
            )
        }

        Text(
            text = "+ 보너스 $bonusNumber",
            modifier = GlanceModifier.padding(top = 6.dp),
            style = TextStyle(color = ColorProvider(color = Color(0xFF4B5563))),
        )
    }
}
