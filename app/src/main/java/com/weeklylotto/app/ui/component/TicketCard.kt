package com.weeklylotto.app.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weeklylotto.app.ui.theme.LottoColors
import com.weeklylotto.app.ui.theme.LottoDimens

@Composable
fun TicketCard(
    title: String,
    numbers: List<Int?>,
    badge: (@Composable () -> Unit)? = null,
    meta: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .let { base ->
                    if (onClick != null) {
                        base
                            .semantics { role = Role.Button }
                            .clickable(onClick = onClick)
                    } else {
                        base
                    }
                },
        shape = RoundedCornerShape(LottoDimens.CardRadius),
        border = BorderStroke(width = 1.dp, color = LottoColors.Border),
        colors = CardDefaults.cardColors(containerColor = LottoColors.Surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LottoColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                badge?.invoke()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LottoDimens.BallGap)) {
                numbers.take(6).forEach { number ->
                    BallChip(number = number, size = LottoDimens.BallSize)
                }
            }
            if (meta != null) {
                Text(
                    text = meta,
                    color = LottoColors.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
