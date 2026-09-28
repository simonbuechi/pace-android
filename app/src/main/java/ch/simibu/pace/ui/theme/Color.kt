package ch.simibu.pace.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary brand colors from Pace logo
val PaceMagenta = Color(0xFF9123A6)
val PaceRaspberry = Color(0xFFD7195F)

val PaceBrandGradient = Brush.linearGradient(
    colors = listOf(PaceMagenta, PaceRaspberry)
)

// Soft tactile / Neumorphic dark palette
val PaceDarkBackground = Color(0xFF16141B)
val PaceDarkSurface = Color(0xFF201C27)
val PaceDarkSurfaceRaised = Color(0xFF282331)
val PaceDarkSurfaceSunken = Color(0xFF131118)
val PaceDarkShadowDark = Color(0xFF0C0A0F)
val PaceDarkShadowLight = Color(0xFF2E2938)
val PaceDarkSurfaceVariant = Color(0xFF2B2535)
val PaceDarkOnBackground = Color(0xFFF3EEF8)
val PaceDarkOnSurfaceVariant = Color(0xFFB0A7BA)

// Soft tactile / Neumorphic light palette
val PaceLightBackground = Color(0xFFF4F1F7)
val PaceLightSurface = Color(0xFFF9F7FB)
val PaceLightSurfaceRaised = Color(0xFFFFFFFF)
val PaceLightSurfaceSunken = Color(0xFFE9E5EE)
val PaceLightSurfaceVariant = Color(0xFFEBE6F2)
val PaceLightShadowDark = Color(0xFFD4CEDC)
val PaceLightShadowLight = Color(0xFFFFFFFF)
val PaceLightOnBackground = Color(0xFF1F1A25)
val PaceLightOnSurfaceVariant = Color(0xFF5D5466)

// Phase specific colors (soft expressive variants)
val PhaseWarmupColor = Color(0xFFFFA000)
val PhaseFocusColor = PaceRaspberry
val PhaseBreakColor = Color(0xFF00BFA5)
val PhaseCompletedColor = PaceMagenta
