package ch.simibu.pace.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import ch.simibu.pace.model.BackgroundAnimationOption
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.ui.theme.PhaseBreakColor
import ch.simibu.pace.ui.theme.PhaseCompletedColor
import ch.simibu.pace.ui.theme.PhaseCooldownColor
import ch.simibu.pace.ui.theme.PhaseWarmupColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

data class TimerBackgroundGradient(
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color
)

fun getTimerBackgroundGradient(
    phase: TimerPhase,
    colorScheme: ColorSchemeOption,
    isDark: Boolean
): TimerBackgroundGradient {
    return when (phase) {
        TimerPhase.BREAK -> {
            if (isDark) {
                TimerBackgroundGradient(
                    topColor = Color(0xFF424754),
                    midColor = Color(0xFF2B2F38),
                    bottomColor = Color(0xFF16181D)
                )
            } else {
                TimerBackgroundGradient(
                    topColor = Color(0xFFE4E8F0),
                    midColor = Color(0xFFD0D6E2),
                    bottomColor = Color(0xFFB8C0D0)
                )
            }
        }
        TimerPhase.WARMUP -> {
            val warmPrimary = PhaseWarmupColor
            val warmSecondary = Color(0xFFFFB74D)
            if (isDark) {
                TimerBackgroundGradient(
                    topColor = warmPrimary.copy(alpha = 0.62f).compositeOver(Color(0xFF18120E)),
                    midColor = warmSecondary.copy(alpha = 0.44f).compositeOver(Color(0xFF120E0A)),
                    bottomColor = warmPrimary.copy(alpha = 0.25f).compositeOver(Color(0xFF0C0907))
                )
            } else {
                TimerBackgroundGradient(
                    topColor = warmPrimary.copy(alpha = 0.46f).compositeOver(Color(0xFFFFFFFF)),
                    midColor = warmSecondary.copy(alpha = 0.30f).compositeOver(Color(0xFFFAFBFD)),
                    bottomColor = warmPrimary.copy(alpha = 0.16f).compositeOver(Color(0xFFF3F5FA))
                )
            }
        }
        TimerPhase.COOLDOWN -> {
            val coolPrimary = PhaseCooldownColor
            val coolSecondary = Color(0xFF80DEEA)
            if (isDark) {
                TimerBackgroundGradient(
                    topColor = coolPrimary.copy(alpha = 0.62f).compositeOver(Color(0xFF0E1618)),
                    midColor = coolSecondary.copy(alpha = 0.44f).compositeOver(Color(0xFF0A1012)),
                    bottomColor = coolPrimary.copy(alpha = 0.25f).compositeOver(Color(0xFF060B0D))
                )
            } else {
                TimerBackgroundGradient(
                    topColor = coolPrimary.copy(alpha = 0.46f).compositeOver(Color(0xFFFFFFFF)),
                    midColor = coolSecondary.copy(alpha = 0.30f).compositeOver(Color(0xFFFAFBFD)),
                    bottomColor = coolPrimary.copy(alpha = 0.16f).compositeOver(Color(0xFFF3F5FA))
                )
            }
        }
        TimerPhase.COMPLETED -> {
            val compPrimary = PhaseCompletedColor
            val compSecondary = Color(0xFF81C784)
            if (isDark) {
                TimerBackgroundGradient(
                    topColor = compPrimary.copy(alpha = 0.66f).compositeOver(Color(0xFF0E1810)),
                    midColor = compSecondary.copy(alpha = 0.46f).compositeOver(Color(0xFF0A120B)),
                    bottomColor = compPrimary.copy(alpha = 0.26f).compositeOver(Color(0xFF060D07))
                )
            } else {
                TimerBackgroundGradient(
                    topColor = compPrimary.copy(alpha = 0.48f).compositeOver(Color(0xFFFFFFFF)),
                    midColor = compSecondary.copy(alpha = 0.32f).compositeOver(Color(0xFFFAFBFD)),
                    bottomColor = compPrimary.copy(alpha = 0.18f).compositeOver(Color(0xFFF3F5FA))
                )
            }
        }
        TimerPhase.FOCUS -> {
            val primary = colorScheme.primaryColor
            val secondary = colorScheme.secondaryColor
            if (isDark) {
                TimerBackgroundGradient(
                    topColor = primary.copy(alpha = 0.66f).compositeOver(Color(0xFF140F19)),
                    midColor = secondary.copy(alpha = 0.46f).compositeOver(Color(0xFF0F0B13)),
                    bottomColor = primary.copy(alpha = 0.26f).compositeOver(Color(0xFF0A070E))
                )
            } else {
                TimerBackgroundGradient(
                    topColor = primary.copy(alpha = 0.48f).compositeOver(Color(0xFFFFFFFF)),
                    midColor = secondary.copy(alpha = 0.32f).compositeOver(Color(0xFFFAFBFD)),
                    bottomColor = primary.copy(alpha = 0.18f).compositeOver(Color(0xFFF3F5FA))
                )
            }
        }
    }
}

@Composable
fun AnimatedTimerBackground(
    animation: BackgroundAnimationOption,
    phase: TimerPhase,
    progress: Float,
    colorScheme: ColorSchemeOption,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val targetGradient = remember(phase, colorScheme, isDark) {
        getTimerBackgroundGradient(phase = phase, colorScheme = colorScheme, isDark = isDark)
    }

    val animatedTopColor by animateColorAsState(
        targetValue = targetGradient.topColor,
        animationSpec = tween(600),
        label = "bg_top_color"
    )
    val animatedMidColor by animateColorAsState(
        targetValue = targetGradient.midColor,
        animationSpec = tween(600),
        label = "bg_mid_color"
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = targetGradient.bottomColor,
        animationSpec = tween(600),
        label = "bg_bottom_color"
    )

    when (animation) {
        BackgroundAnimationOption.NONE,
        BackgroundAnimationOption.APP_DEFAULT -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                val verticalBrush = Brush.verticalGradient(
                    0.0f to animatedTopColor,
                    0.55f to animatedMidColor,
                    1.0f to animatedBottomColor
                )
                drawRect(brush = verticalBrush)
            }
        }

        BackgroundAnimationOption.BREATHING_AURA -> {
            val infiniteTransition = rememberInfiniteTransition(label = "breathing_aura")
            val breathCycle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "breath_cycle"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val maxDim = max(w, h)

                // Base vertical gradient
                val verticalBrush = Brush.verticalGradient(
                    0.0f to animatedTopColor,
                    0.55f to animatedMidColor,
                    1.0f to animatedBottomColor
                )
                drawRect(brush = verticalBrush)

                // Pulsing ambient radial aura
                val pulseRatio = (sin(breathCycle) + 1f) / 2f // 0f..1f
                val auraRadius = maxDim * (0.55f + 0.25f * pulseRatio)
                val auraCenter = Offset(w * 0.5f, h * (0.42f + 0.04f * sin(breathCycle * 0.5f)))
                val auraAlpha = if (isDark) 0.35f + 0.20f * pulseRatio else 0.25f + 0.15f * pulseRatio

                val auraBrush = Brush.radialGradient(
                    0.0f to animatedTopColor.copy(alpha = auraAlpha),
                    0.55f to animatedMidColor.copy(alpha = auraAlpha * 0.6f),
                    1.0f to Color.Transparent,
                    center = auraCenter,
                    radius = auraRadius
                )
                drawRect(brush = auraBrush)
            }
        }

        BackgroundAnimationOption.AURORA_FLOW -> {
            val infiniteTransition = rememberInfiniteTransition(label = "aurora_flow")
            val flowPhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 11000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "flow_phase"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base vertical gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to animatedTopColor,
                        0.55f to animatedMidColor,
                        1.0f to animatedBottomColor
                    )
                )

                // Aurora lobe 1: Drifting from top-left towards center-right
                val lobe1X = w * (0.35f + 0.25f * cos(flowPhase))
                val lobe1Y = h * (0.30f + 0.20f * sin(flowPhase))
                val lobe1Radius = max(w, h) * (0.60f + 0.15f * sin(flowPhase * 1.5f))
                val alpha1 = if (isDark) 0.38f else 0.26f

                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to animatedMidColor.copy(alpha = alpha1),
                        0.60f to animatedTopColor.copy(alpha = alpha1 * 0.4f),
                        1.0f to Color.Transparent,
                        center = Offset(lobe1X, lobe1Y),
                        radius = lobe1Radius
                    )
                )

                // Aurora lobe 2: Counter-drifting ambient glow
                val lobe2X = w * (0.65f - 0.25f * sin(flowPhase * 0.8f))
                val lobe2Y = h * (0.65f - 0.15f * cos(flowPhase * 0.8f))
                val lobe2Radius = max(w, h) * (0.55f + 0.15f * cos(flowPhase))
                val alpha2 = if (isDark) 0.32f else 0.22f

                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to animatedTopColor.copy(alpha = alpha2),
                        0.50f to animatedBottomColor.copy(alpha = alpha2 * 0.35f),
                        1.0f to Color.Transparent,
                        center = Offset(lobe2X, lobe2Y),
                        radius = lobe2Radius
                    )
                )
            }
        }

        BackgroundAnimationOption.HORIZON_GLOW -> {
            val smoothProgress by animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "horizon_progress"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Horizon rise from bottom up as timer elapses
                // Progress 0: glow at y = 1.0 (bottom edge)
                // Progress 1: glow rises to y = 0.1 (top edge)
                val horizonY = h * (1.0f - 0.90f * smoothProgress)
                val glowSpread = h * (0.45f + 0.35f * smoothProgress)

                // Base gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to animatedTopColor,
                        0.55f to animatedMidColor,
                        1.0f to animatedBottomColor
                    )
                )

                // Radiant rising horizon aura
                val glowAlpha = if (isDark) 0.48f else 0.34f
                val horizonBrush = Brush.radialGradient(
                    0.0f to animatedTopColor.copy(alpha = glowAlpha),
                    0.45f to animatedMidColor.copy(alpha = glowAlpha * 0.65f),
                    1.0f to Color.Transparent,
                    center = Offset(w * 0.5f, horizonY),
                    radius = glowSpread
                )
                drawRect(brush = horizonBrush)
            }
        }

        BackgroundAnimationOption.METALLIC_SHEEN -> {
            val infiniteTransition = rememberInfiniteTransition(label = "metallic_sheen")
            val sweepProgress by infiniteTransition.animateFloat(
                initialValue = -0.6f,
                targetValue = 1.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sheen_sweep"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to animatedTopColor,
                        0.55f to animatedMidColor,
                        1.0f to animatedBottomColor
                    )
                )

                // Diagonal specular highlight band
                val sheenCenter = Offset(w * sweepProgress, h * sweepProgress)
                val sheenStart = Offset(sheenCenter.x - w * 0.45f, sheenCenter.y - h * 0.45f)
                val sheenEnd = Offset(sheenCenter.x + w * 0.45f, sheenCenter.y + h * 0.45f)

                val highlightColor = if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.42f)

                val sheenBrush = Brush.linearGradient(
                    0.0f to Color.Transparent,
                    0.40f to Color.Transparent,
                    0.50f to highlightColor,
                    0.60f to Color.Transparent,
                    1.0f to Color.Transparent,
                    start = sheenStart,
                    end = sheenEnd
                )
                drawRect(brush = sheenBrush)
            }
        }

        BackgroundAnimationOption.SENSOR_PARALLAX -> {
            val context = LocalContext.current
            var rawTiltX by remember { mutableFloatStateOf(0f) }
            var rawTiltY by remember { mutableFloatStateOf(0f) }

            val smoothTiltX = remember { Animatable(0f) }
            val smoothTiltY = remember { Animatable(0f) }

            // Infinite idle drift fallback when sensor is motionless or unavailable
            val infiniteTransition = rememberInfiniteTransition(label = "idle_drift")
            val idlePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "idle_phase"
            )

            DisposableEffect(context) {
                val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
                val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
                    ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.values?.let { v ->
                            // Gravity / Accel: X is left/right (-9.8 to +9.8), Y is vertical tilt (-9.8 to +9.8)
                            val nx = (-v[0] / 9.8f).coerceIn(-1f, 1f)
                            val ny = (v[1] / 9.8f).coerceIn(-1f, 1f)
                            rawTiltX = nx
                            rawTiltY = ny
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                if (sensor != null && sensorManager != null) {
                    sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                }

                onDispose {
                    sensorManager?.unregisterListener(listener)
                }
            }

            LaunchedEffect(rawTiltX, rawTiltY) {
                smoothTiltX.animateTo(rawTiltX, tween(250, easing = LinearEasing))
            }
            LaunchedEffect(rawTiltY) {
                smoothTiltY.animateTo(rawTiltY, tween(250, easing = LinearEasing))
            }

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Combine tilt with a tiny ambient drift so it stays visually alive even stationary
                val combinedTiltX = smoothTiltX.value + 0.08f * cos(idlePhase)
                val combinedTiltY = smoothTiltY.value + 0.08f * sin(idlePhase)

                val startOffset = Offset(
                    x = w * (0.5f - 0.35f * combinedTiltX),
                    y = 0f + h * 0.15f * combinedTiltY
                )
                val endOffset = Offset(
                    x = w * (0.5f + 0.35f * combinedTiltX),
                    y = h + h * 0.15f * combinedTiltY
                )

                // Tilt-shifted linear gradient
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to animatedTopColor,
                        0.55f to animatedMidColor,
                        1.0f to animatedBottomColor,
                        start = startOffset,
                        end = endOffset
                    )
                )

                // Dynamic light reflection bloom tracking tilt
                val bloomCenter = Offset(
                    x = w * (0.5f + 0.40f * combinedTiltX),
                    y = h * (0.45f - 0.35f * combinedTiltY)
                )
                val bloomRadius = max(w, h) * 0.65f
                val bloomAlpha = if (isDark) 0.30f else 0.22f

                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to animatedTopColor.copy(alpha = bloomAlpha),
                        0.60f to Color.Transparent,
                        center = bloomCenter,
                        radius = bloomRadius
                    )
                )
            }
        }
    }
}
