package com.weeklylotto.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LocalMotionSettings
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens

data class LottoBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun LottoBottomBar(
    items: List<LottoBottomBarItem>,
    currentRoute: String?,
    onSelect: (LottoBottomBarItem) -> Unit,
) {
    val motionSettings = LocalMotionSettings.current
    val durationMillis = motionSettings.durationMillis(defaultMillis = 140)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(LottoColors.Surface),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LottoColors.Border),
        ) {}
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(LottoDimens.BottomBarHeight)
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute
                val tint by
                    animateColorAsState(
                        targetValue = if (selected) LottoColors.Primary else LottoColors.TextMuted,
                        animationSpec = tween(durationMillis = durationMillis),
                        label = "bottomBarTint",
                    )
                val scale by
                    animateFloatAsState(
                        targetValue = if (selected) 1f else 0.92f,
                        animationSpec = tween(durationMillis = durationMillis),
                        label = "bottomBarScale",
                    )
                val alpha by
                    animateFloatAsState(
                        targetValue = if (selected) 1f else 0.82f,
                        animationSpec = tween(durationMillis = durationMillis),
                        label = "bottomBarAlpha",
                    )
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .graphicsLayer {
                                this.alpha = alpha
                                if (!motionSettings.reduceMotionEnabled) {
                                    scaleX = scale
                                    scaleY = scale
                                }
                            }
                            .clickable { onSelect(item) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(imageVector = item.icon, contentDescription = item.label, tint = tint)
                    Text(
                        text = item.label,
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}
