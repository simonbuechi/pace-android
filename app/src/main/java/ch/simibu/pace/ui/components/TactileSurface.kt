package ch.simibu.pace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.simibu.pace.ui.theme.PaceDarkShadowDark
import ch.simibu.pace.ui.theme.PaceDarkShadowLight
import ch.simibu.pace.ui.theme.PaceDarkSurface
import ch.simibu.pace.ui.theme.PaceDarkSurfaceRaised
import ch.simibu.pace.ui.theme.PaceDarkSurfaceSunken
import ch.simibu.pace.ui.theme.PaceLightShadowDark
import ch.simibu.pace.ui.theme.PaceLightShadowLight
import ch.simibu.pace.ui.theme.PaceLightSurface
import ch.simibu.pace.ui.theme.PaceLightSurfaceRaised
import ch.simibu.pace.ui.theme.PaceLightSurfaceSunken

@Composable
fun isTactileThemeDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()
    val bgColor = if (isDark) PaceDarkSurface else PaceLightSurface
    val highlightColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.95f)
    val shadowBorderColor = if (isDark) Color.Black.copy(alpha = 0.30f) else Color(0xFFDCD6E5).copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) PaceDarkShadowDark else PaceLightShadowDark,
                spotColor = if (isDark) PaceDarkShadowDark else PaceLightShadowDark
            )
            .clip(shape)
            .background(bgColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(highlightColor, shadowBorderColor)
                ),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun TactileSunkenWell(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()
    val sunkenBg = if (isDark) PaceDarkSurfaceSunken else PaceLightSurfaceSunken
    val topShadowBorder = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f)
    val bottomHighlight = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(sunkenBg)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(topShadowBorder, bottomHighlight)
                ),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun TactilePillButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isTactileThemeDark()
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color(0xFFD7195F).copy(alpha = 0.45f) else Color(0xFFD7195F).copy(alpha = 0.30f),
                spotColor = Color(0xFF9123A6).copy(alpha = 0.55f)
            )
            .clip(shape),
        content = content
    )
}
