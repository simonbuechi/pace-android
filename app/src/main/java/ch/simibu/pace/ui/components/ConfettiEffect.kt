package ch.simibu.pace.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val isCircle: Boolean
)

/**
 * Playful, lightweight confetti burst effect implemented with pure Compose Canvas.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 55,
    durationMs: Int = 3200
) {
    val progress = remember { Animatable(0f) }

    val confettiColors = remember {
        listOf(
            PaceMagenta,
            PaceRaspberry,
            Color(0xFF00B074), // Emerald
            Color(0xFFFF9100), // Amber
            Color(0xFF7C4DFF), // Violet
            Color(0xFF0288D1), // Ocean
            Color(0xFFFF5252), // Coral
            Color(0xFFFFD700)  // Gold
        )
    }

    val particles = remember {
        val random = Random(42)
        List(particleCount) {
            val angle = random.nextDouble(Math.PI * 0.15, Math.PI * 0.85) // bursting upward
            val speed = random.nextDouble(450.0, 1150.0).toFloat()
            ConfettiParticle(
                startX = random.nextFloat() * 0.4f + 0.3f, // centered burst origin
                startY = random.nextFloat() * 0.2f + 0.35f,
                velocityX = (cos(angle) * speed).toFloat() * if (random.nextBoolean()) 1f else -1f,
                velocityY = -(sin(angle) * speed).toFloat(),
                color = confettiColors[random.nextInt(confettiColors.size)],
                size = random.nextFloat() * 10f + 9f,
                rotationSpeed = (random.nextFloat() * 720f - 360f),
                initialRotation = random.nextFloat() * 360f,
                isCircle = random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        val t = progress.value
        val gravity = 900f // px/sec^2
        val alpha = if (t > 0.65f) ((1f - t) / 0.35f).coerceIn(0f, 1f) else 1f

        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            for (p in particles) {
                val timeSec = t * (durationMs / 1000f)
                val curX = p.startX * width + p.velocityX * timeSec
                val curY = p.startY * height + p.velocityY * timeSec + 0.5f * gravity * timeSec * timeSec
                val curRot = p.initialRotation + p.rotationSpeed * timeSec

                if (curX in -50f..(width + 50f) && curY in -50f..(height + 50f)) {
                    withTransform({
                        translate(curX, curY)
                        rotate(curRot)
                    }) {
                        if (p.isCircle) {
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                radius = p.size / 2f,
                                center = Offset.Zero
                            )
                        } else {
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(-p.size / 2f, -p.size / 3f),
                                size = Size(p.size, p.size * 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}
