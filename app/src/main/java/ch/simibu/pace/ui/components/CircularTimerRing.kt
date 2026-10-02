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
import androidx.compose.foundation.isSystemInDarkTheme
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
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.ui.theme.PaceDarkSurfaceSunken
import ch.simibu.pace.ui.theme.PaceLightSurfaceSunken
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry
import ch.simibu.pace.ui.theme.PhaseBreakColor
import ch.simibu.pace.ui.theme.PhaseCompletedColor
import ch.simibu.pace.ui.theme.PhaseCooldownColor
import ch.simibu.pace.ui.theme.PhaseFocusColor
import ch.simibu.pace.ui.theme.PhaseWarmupColor

@Composable
fun CircularTimerRing(
    modifier: Modifier = Modifier,
    progress: Float,
    phase: TimerPhase,
    remainingSeconds: Int,
    accentColor: Color = PaceRaspberry,
    colorScheme: ColorSchemeOption = ColorSchemeOption.PACE,
    strokeWidth: Dp = 16.dp,
    isMuted: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    val isDark = isTactileThemeDark()

    val targetColor = when (phase) {
        TimerPhase.WARMUP -> PhaseWarmupColor
        TimerPhase.FOCUS -> colorScheme.primaryColor
        TimerPhase.BREAK -> if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
        TimerPhase.COOLDOWN -> PhaseCooldownColor
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
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        animateFloatAsState(1.0f, label = "idle_scale")
    }

    val baseTrackBg = if (isDark) PaceDarkSurfaceSunken else PaceLightSurfaceSunken
    val trackBgColor = if (isMuted) baseTrackBg.copy(alpha = if (isDark) 0.40f else 0.50f) else baseTrackBg
    val trackBorderColor = if (isDark) Color.White.copy(alpha = if (isMuted) 0.03f else 0.06f) else Color.Black.copy(alpha = if (isMuted) 0.03f else 0.05f)

    val targetColors = when (phase) {
        TimerPhase.WARMUP -> listOf(PhaseWarmupColor, Color(0xFFFFB74D))
        TimerPhase.FOCUS -> listOf(colorScheme.primaryColor, colorScheme.secondaryColor)
        TimerPhase.BREAK -> if (isDark) {
            listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
        } else {
            listOf(Color(0xFF475569), Color(0xFF64748B))
        }
        TimerPhase.COOLDOWN -> listOf(PhaseCooldownColor, Color(0xFF80DEEA))
        TimerPhase.COMPLETED -> listOf(PhaseCompletedColor, Color(0xFF81C784))
    }

    val arcAlpha = if (isMuted) (if (isDark) 0.32f else 0.38f) else 1.0f

    Box(
        modifier = modifier
            .scale(pulseScale)
            .padding(10.dp),
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

            // Outer tactile shadow ring
            drawArc(
                color = trackBorderColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeft.x - 1.5f, topLeft.y - 1.5f),
                size = Size(arcSize.width + 3f, arcSize.height + 3f),
                style = Stroke(width = strokePx + 3f, cap = StrokeCap.Round)
            )

            // Background Sunken Track
            drawArc(
                color = trackBgColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc with Muted Tactile Gradient
            val sweep = animatedProgress * 360f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to targetColors[0].copy(alpha = arcAlpha),
                        0.5f to targetColors[1].copy(alpha = arcAlpha),
                        1f to targetColors[0].copy(alpha = arcAlpha)
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

        // Optional Inner Content
        content?.invoke()
    }
}
