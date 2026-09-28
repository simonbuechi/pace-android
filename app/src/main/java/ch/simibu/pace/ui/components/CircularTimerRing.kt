package ch.simibu.pace.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.ui.theme.PhaseBreakColor
import ch.simibu.pace.ui.theme.PhaseCompletedColor
import ch.simibu.pace.ui.theme.PhaseFocusColor
import ch.simibu.pace.ui.theme.PhaseWarmupColor

@Composable
fun CircularTimerRing(
    modifier: Modifier = Modifier,
    progress: Float,
    phase: TimerPhase,
    remainingSeconds: Int,
    accentColor: Color,
    strokeWidth: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    val targetColor = when (phase) {
        TimerPhase.WARMUP -> PhaseWarmupColor
        TimerPhase.FOCUS -> accentColor
        TimerPhase.BREAK -> PhaseBreakColor
        TimerPhase.COMPLETED -> PhaseCompletedColor
    }

    val animatedPhaseColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "phase_color"
    )

    // Pulse animation during the last 3 seconds of a phase
    val isWarningCountdown = remainingSeconds in 1..3 && phase != TimerPhase.COMPLETED
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by if (isWarningCountdown) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        animateFloatAsState(1.0f, label = "idle_scale")
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .scale(pulseScale)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            val sweep = animatedProgress * 360f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            animatedPhaseColor.copy(alpha = 0.85f),
                            animatedPhaseColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Inner Content
        content()
    }
}
