package ch.simibu.pace.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Playful expanding ripple wave that pulses on phase transitions or timer start.
 */
@Composable
fun PhaseRippleEffect(
    triggerKey: Any,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scale = remember(triggerKey) { Animatable(0.60f) }
    val alpha = remember(triggerKey) { Animatable(0.60f) }

    LaunchedEffect(triggerKey) {
        scale.animateTo(
            targetValue = 1.35f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(triggerKey) {
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    if (alpha.value > 0.01f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.75f
            val currentRadius = baseRadius * scale.value

            // Soft glowing bloom disk
            drawCircle(
                color = color.copy(alpha = alpha.value * 0.22f),
                radius = currentRadius,
                center = center
            )
            // Crisp expanding wave crest
            drawCircle(
                color = color.copy(alpha = alpha.value * 0.75f),
                radius = currentRadius,
                center = center,
                style = Stroke(width = 8f * (1f - (scale.value - 0.60f) / 0.75f).coerceIn(0.2f, 1f))
            )
        }
    }
}
