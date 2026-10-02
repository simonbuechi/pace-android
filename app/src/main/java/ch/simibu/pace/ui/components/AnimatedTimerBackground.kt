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

    // Vibrant accent colors for high-contrast animated highlights and fluid waves
    val activeAccentColor = when (phase) {
        TimerPhase.BREAK -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        TimerPhase.WARMUP -> PhaseWarmupColor
        TimerPhase.FOCUS -> colorScheme.primaryColor
        TimerPhase.COOLDOWN -> PhaseCooldownColor
        TimerPhase.COMPLETED -> PhaseCompletedColor
    }
    val activeSecondaryColor = when (phase) {
        TimerPhase.BREAK -> if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
        TimerPhase.WARMUP -> Color(0xFFFFB74D)
        TimerPhase.FOCUS -> colorScheme.secondaryColor
        TimerPhase.COOLDOWN -> Color(0xFF80DEEA)
        TimerPhase.COMPLETED -> Color(0xFF81C784)
    }

    // Resolve APP_DEFAULT to BREATHING_AURA if not already resolved
    val effectiveAnimation = if (animation == BackgroundAnimationOption.APP_DEFAULT) {
        BackgroundAnimationOption.BREATHING_AURA
    } else {
        animation
    }

    when (effectiveAnimation) {
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
            val breathProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breath_progress"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val maxDim = max(w, h)

                // 1. Dynamic base gradient that subtly breathes vertically
                val topShift = h * 0.12f * (breathProgress - 0.5f)
                val baseBrush = Brush.verticalGradient(
                    0.0f to animatedTopColor,
                    (0.45f + 0.15f * breathProgress) to animatedMidColor,
                    1.0f to animatedBottomColor,
                    startY = topShift,
                    endY = h + topShift
                )
                drawRect(brush = baseBrush)

                // 2. High-contrast pulsing radial breathing aura centered behind the clock
                val centerOffset = Offset(
                    x = w * 0.5f,
                    y = h * 0.46f + h * 0.04f * (1f - breathProgress)
                )
                // Radius expands from 45% of screen up to 105% of screen!
                val auraRadius = maxDim * (0.45f + 0.60f * breathProgress)
                val auraAlpha = if (isDark) 0.50f + 0.35f * breathProgress else 0.40f + 0.30f * breathProgress

                val auraBrush = Brush.radialGradient(
                    0.0f to activeAccentColor.copy(alpha = auraAlpha),
                    0.45f to animatedMidColor.copy(alpha = auraAlpha * 0.65f),
                    1.0f to Color.Transparent,
                    center = centerOffset,
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
                    animation = tween(durationMillis = 9000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "flow_phase"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val maxDim = max(w, h)

                // 1. Rotating, shifting orbital linear gradient
                val angle = flowPhase
                val start = Offset(
                    x = w * (0.5f + 0.50f * cos(angle)),
                    y = h * (0.5f + 0.50f * sin(angle))
                )
                val end = Offset(
                    x = w * (0.5f - 0.50f * cos(angle)),
                    y = h * (0.5f - 0.50f * sin(angle))
                )

                val rotatingBrush = Brush.linearGradient(
                    0.0f to animatedTopColor,
                    (0.40f + 0.20f * sin(flowPhase)) to animatedMidColor,
                    1.0f to animatedBottomColor,
                    start = start,
                    end = end
                )
                drawRect(brush = rotatingBrush)

                // 2. Flowing Aurora Lobe 1 (Primary Accent) orbiting smoothly
                val lobe1X = w * (0.45f + 0.38f * sin(flowPhase))
                val lobe1Y = h * (0.40f + 0.28f * cos(flowPhase * 0.8f))
                val lobe1Radius = maxDim * (0.55f + 0.15f * sin(flowPhase * 1.4f))
                val lobe1Alpha = if (isDark) 0.52f else 0.40f

                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to activeAccentColor.copy(alpha = lobe1Alpha),
                        0.55f to animatedMidColor.copy(alpha = lobe1Alpha * 0.45f),
                        1.0f to Color.Transparent,
                        center = Offset(lobe1X, lobe1Y),
                        radius = lobe1Radius
                    )
                )

                // 3. Counter-drifting Aurora Lobe 2 (Secondary Accent)
                val lobe2X = w * (0.55f - 0.38f * cos(flowPhase * 0.7f))
                val lobe2Y = h * (0.60f - 0.25f * sin(flowPhase * 0.9f))
                val lobe2Radius = maxDim * (0.50f + 0.15f * cos(flowPhase))
                val lobe2Alpha = if (isDark) 0.46f else 0.35f

                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to activeSecondaryColor.copy(alpha = lobe2Alpha),
                        0.50f to Color.Transparent,
                        center = Offset(lobe2X, lobe2Y),
                        radius = lobe2Radius
                    )
                )
            }
        }

        BackgroundAnimationOption.HORIZON_GLOW -> {
            val smoothProgress by animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "horizon_progress"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val maxDim = max(w, h)

                // Horizon line physically rises from y = 1.05 (bottom) to y = 0.05 (top) as time elapses
                val horizonY = h * (1.05f - 0.95f * smoothProgress)
                val spreadRadius = maxDim * (0.50f + 0.40f * smoothProgress)

                // 1. Progress-driven vertical gradient where radiant top color takes over screen
                val progressBrush = Brush.verticalGradient(
                    0.0f to animatedBottomColor,
                    (horizonY / h).coerceIn(0.1f, 0.9f) to animatedMidColor,
                    1.0f to animatedTopColor
                )
                drawRect(brush = progressBrush)

                // 2. High-intensity glowing horizon dawn bloom rising upwards
                val glowAlpha = if (isDark) 0.60f else 0.46f
                val horizonGlowBrush = Brush.radialGradient(
                    0.0f to activeAccentColor.copy(alpha = glowAlpha),
                    0.40f to activeSecondaryColor.copy(alpha = glowAlpha * 0.65f),
                    1.0f to Color.Transparent,
                    center = Offset(w * 0.5f, horizonY),
                    radius = spreadRadius
                )
                drawRect(brush = horizonGlowBrush)
            }
        }

        BackgroundAnimationOption.METALLIC_SHEEN -> {
            val infiniteTransition = rememberInfiniteTransition(label = "metallic_sheen")
            val sweepProgress by infiniteTransition.animateFloat(
                initialValue = -0.4f,
                targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sheen_sweep"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val maxDim = max(w, h)

                // 1. Base vibrant gradient
                val baseBrush = Brush.verticalGradient(
                    0.0f to animatedTopColor,
                    0.55f to animatedMidColor,
                    1.0f to animatedBottomColor
                )
                drawRect(brush = baseBrush)

                // 2. Specular metallic reflection beam sweeping across diagonal
                val sheenCenter = Offset(w * sweepProgress, h * sweepProgress)
                val bandSpan = maxDim * 0.35f
                val sheenStart = Offset(sheenCenter.x - bandSpan, sheenCenter.y - bandSpan)
                val sheenEnd = Offset(sheenCenter.x + bandSpan, sheenCenter.y + bandSpan)

                val highlightColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.65f)
                val sheenGlow = activeSecondaryColor.copy(alpha = if (isDark) 0.35f else 0.40f)

                val sheenBrush = Brush.linearGradient(
                    0.0f to Color.Transparent,
                    0.30f to Color.Transparent,
                    0.45f to sheenGlow,
                    0.50f to highlightColor,
                    0.55f to sheenGlow,
                    0.70f to Color.Transparent,
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

            // Continuous ambient oscillation keeping background alive when resting
            val infiniteTransition = rememberInfiniteTransition(label = "idle_drift")
            val idlePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 6000, easing = LinearEasing),
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

            LaunchedEffect(rawTiltX) {
                smoothTiltX.animateTo(rawTiltX, tween(200, easing = LinearEasing))
            }
            LaunchedEffect(rawTiltY) {
                smoothTiltY.animateTo(rawTiltY, tween(200, easing = LinearEasing))
            }

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val maxDim = max(w, h)

                val combinedTiltX = (smoothTiltX.value + 0.15f * cos(idlePhase)).coerceIn(-1.2f, 1.2f)
                val combinedTiltY = (smoothTiltY.value + 0.15f * sin(idlePhase)).coerceIn(-1.2f, 1.2f)

                // Start and end coordinates of gradient tilt dynamically with device angle
                val startOffset = Offset(
                    x = w * (0.5f - 0.55f * combinedTiltX),
                    y = 0f + h * 0.35f * combinedTiltY
                )
                val endOffset = Offset(
                    x = w * (0.5f + 0.55f * combinedTiltX),
                    y = h + h * 0.35f * combinedTiltY
                )

                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to animatedTopColor,
                        0.50f to animatedMidColor,
                        1.0f to animatedBottomColor,
                        start = startOffset,
                        end = endOffset
                    )
                )

                // Dynamic light reflection bloom tracking tilt
                val bloomCenter = Offset(
                    x = w * (0.5f + 0.50f * combinedTiltX),
                    y = h * (0.45f - 0.40f * combinedTiltY)
                )
                val bloomAlpha = if (isDark) 0.50f else 0.38f

                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to activeAccentColor.copy(alpha = bloomAlpha),
                        0.45f to activeSecondaryColor.copy(alpha = bloomAlpha * 0.5f),
                        1.0f to Color.Transparent,
                        center = bloomCenter,
                        radius = maxDim * 0.65f
                    )
                )
            }
        }
    }
}
