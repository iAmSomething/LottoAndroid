package com.weeklylotto.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.weeklylotto.app.ui.theme.LocalMotionSettings

@Composable
fun MotionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val motionSettings = LocalMotionSettings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale =
        if (enabled && isPressed && !motionSettings.reduceMotionEnabled) {
            0.98f
        } else {
            1f
        }
    val scale by
        animateFloatAsState(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = motionSettings.durationMillis(defaultMillis = 80, minMillis = 40)),
            label = "motionButtonScale",
        )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier =
            modifier
                .graphicsLayer {
                    if (!motionSettings.reduceMotionEnabled || scale != 1f) {
                        scaleX = scale
                        scaleY = scale
                    }
                },
    ) {
        content()
    }
}

@Composable
fun MotionTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val motionSettings = LocalMotionSettings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale =
        if (enabled && isPressed && !motionSettings.reduceMotionEnabled) {
            0.98f
        } else {
            1f
        }
    val scale by
        animateFloatAsState(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = motionSettings.durationMillis(defaultMillis = 80, minMillis = 40)),
            label = "motionTextButtonScale",
        )

    TextButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier =
            modifier
                .graphicsLayer {
                    if (!motionSettings.reduceMotionEnabled || scale != 1f) {
                        scaleX = scale
                        scaleY = scale
                    }
                },
    ) {
        content()
    }
}
