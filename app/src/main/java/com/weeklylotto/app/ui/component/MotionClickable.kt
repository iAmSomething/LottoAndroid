package com.weeklylotto.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.weeklylotto.app.ui.theme.LocalMotionSettings

@Composable
fun Modifier.motionClickable(
    enabled: Boolean = true,
    role: Role? = null,
    minScale: Float = 0.98f,
    onClick: () -> Unit,
): Modifier =
    composed {
        val motionSettings = LocalMotionSettings.current
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val targetScale =
            if (enabled && isPressed && !motionSettings.reduceMotionEnabled) {
                minScale
            } else {
                1f
            }
        val scale by
            animateFloatAsState(
                targetValue = targetScale,
                animationSpec =
                    tween(
                        durationMillis = motionSettings.durationMillis(defaultMillis = 80, minMillis = 40),
                    ),
                label = "motionClickableScale",
            )

        this
            .graphicsLayer {
                if (!motionSettings.reduceMotionEnabled || scale != 1f) {
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clickable(
                enabled = enabled,
                role = role,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
    }
