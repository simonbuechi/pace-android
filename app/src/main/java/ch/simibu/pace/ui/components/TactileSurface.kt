package ch.simibu.pace.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry

@Composable
fun isTactileThemeDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/**
 * Extruded convex tactile card with dual-light directional shading,
 * soft ambient drop shadow, and micro-beveled light specular rim.
 */
@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()

    // 135-degree convex surface gradient (lightest at top-left, deeper at bottom-right)
    val surfaceGradient = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF2B2536), // lit top-left
                Color(0xFF1E1A25), // mid
                Color(0xFF16131D)  // shaded bottom-right
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF), // specular white top-left
                Color(0xFFF9F7FB), // warm neutral mid
                Color(0xFFE9E4F0)  // soft shaded bottom-right
            )
        )
    }

    // Two-tone micro-bevel border (specular catch-light top-left, shadow rim bottom-right)
    val borderGradient = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.16f),
                Color.Black.copy(alpha = 0.55f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFD0C8D8).copy(alpha = 0.85f)
            )
        )
    }

    // Shadow colors tuned for rich depth
    val ambientColor = if (isDark) Color(0xFF09070D).copy(alpha = 0.95f) else Color(0xFF6B5F7A).copy(alpha = 0.35f)
    val spotColor = if (isDark) Color.Black.copy(alpha = 0.90f) else Color(0xFF4C4258).copy(alpha = 0.40f)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ambientColor,
                spotColor = spotColor
            )
            .clip(shape)
            .background(surfaceGradient)
            .border(
                width = 1.5.dp,
                brush = borderGradient,
                shape = shape
            ),
        content = content
    )
}

/**
 * Debossed concave sunken well for recessed panels, counters, wheel pickers, and readouts.
 * Inverts the lighting: deep top-left cavity shadow, faint bottom-right specular catch-light.
 */
@Composable
fun TactileSunkenWell(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()

    // Inverted 135-degree concave surface gradient (darkest at top-left cavity, lifted at bottom-right)
    val sunkenGradient = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF100E15), // deep shadow top-left
                Color(0xFF17141E), // mid
                Color(0xFF221D2C)  // light bounce bottom-right
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFDFD9E6), // shadow top-left
                Color(0xFFECE7F2), // mid
                Color(0xFFFBF9FC)  // light bounce bottom-right
            )
        )
    }

    // Inset cavity border rim: dark top-left, specular catch-rim bottom-right
    val insetBorderGradient = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.70f),
                Color.White.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF9E94A8).copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.90f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(sunkenGradient)
            .border(
                width = 1.5.dp,
                brush = insetBorderGradient,
                shape = shape
            ),
        content = content
    )
}

/**
 * Interactive physical tactile button with dynamic press physics:
 * Raised convex state when idle, collapsing into surface with scale punch when pressed.
 */
@Composable
fun TactilePillButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 10.dp,
    useBrandGradient: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Dynamic spring physics for tactile press
    val currentElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else elevation,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "button_elevation"
    )

    val currentScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "button_scale"
    )

    val backgroundBrush = if (useBrandGradient) {
        PaceBrandGradient
    } else {
        if (isDark) {
            Brush.linearGradient(listOf(Color(0xFF352D42), Color(0xFF211C2A)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEBE6F3)))
        }
    }

    val ambientShadow = if (useBrandGradient) {
        PaceRaspberry.copy(alpha = if (isDark) 0.55f else 0.40f)
    } else {
        if (isDark) Color(0xFF09070D).copy(alpha = 0.9f) else Color(0xFF6B5F7A).copy(alpha = 0.35f)
    }

    val spotShadow = if (useBrandGradient) {
        PaceMagenta.copy(alpha = if (isDark) 0.65f else 0.50f)
    } else {
        if (isDark) Color.Black.copy(alpha = 0.95f) else Color(0xFF4C4258).copy(alpha = 0.45f)
    }

    val borderGradient = if (useBrandGradient) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isPressed) 0.15f else 0.45f),
                Color.Black.copy(alpha = 0.30f)
            )
        )
    } else {
        if (isDark) {
            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Black.copy(alpha = 0.60f)))
        } else {
            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.98f), Color(0xFFD0C8D8).copy(alpha = 0.85f)))
        }
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null, // Custom tactile spring handles visual feedback
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(currentScale)
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = ambientShadow,
                spotColor = spotShadow
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = 1.5.dp,
                brush = borderGradient,
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

/**
 * Circular extruded tactile puck for steppers (+/-) and icon actions.
 * Depresses into surface on click with spring animation.
 */
@Composable
fun TactileCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else elevation,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "circle_button_elevation"
    )

    val currentScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "circle_button_scale"
    )

    val surfaceGradient = if (isDark) {
        if (isPressed) {
            Brush.linearGradient(listOf(Color(0xFF141219), Color(0xFF221D2C)))
        } else {
            Brush.linearGradient(listOf(Color(0xFF2F293B), Color(0xFF1E1A26)))
        }
    } else {
        if (isPressed) {
            Brush.linearGradient(listOf(Color(0xFFE2DCE8), Color(0xFFFBF9FC)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFECE7F4)))
        }
    }

    val borderGradient = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isPressed) 0.05f else 0.20f),
                Color.Black.copy(alpha = 0.60f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isPressed) 0.40f else 0.98f),
                Color(0xFFCEC6D6).copy(alpha = 0.80f)
            )
        )
    }

    val ambientColor = if (isDark) Color(0xFF09070D).copy(alpha = 0.95f) else Color(0xFF6B5F7A).copy(alpha = 0.35f)
    val spotColor = if (isDark) Color.Black.copy(alpha = 0.90f) else Color(0xFF4C4258).copy(alpha = 0.40f)

    Box(
        modifier = modifier
            .scale(currentScale)
            .shadow(
                elevation = currentElevation,
                shape = CircleShape,
                ambientColor = ambientColor,
                spotColor = spotColor
            )
            .clip(CircleShape)
            .background(surfaceGradient)
            .border(
                width = 1.5.dp,
                brush = borderGradient,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
