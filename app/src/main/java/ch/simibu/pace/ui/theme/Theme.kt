package ch.simibu.pace.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ch.simibu.pace.model.ColorSchemeOption

private fun createDarkColorScheme(accent: ColorSchemeOption) = darkColorScheme(
    primary = accent.primaryColor,
    secondary = accent.secondaryColor,
    tertiary = accent.secondaryColor.copy(alpha = 0.8f),
    background = PaceDarkBackground,
    surface = PaceDarkSurface,
    surfaceVariant = PaceDarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = PaceDarkOnBackground,
    onSurface = PaceDarkOnBackground,
    onSurfaceVariant = Color(0xFFC4C7C5)
)

private fun createLightColorScheme(accent: ColorSchemeOption) = lightColorScheme(
    primary = accent.primaryColor,
    secondary = accent.secondaryColor,
    tertiary = accent.secondaryColor.copy(alpha = 0.8f),
    background = PaceLightBackground,
    surface = PaceLightSurface,
    surfaceVariant = PaceLightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PaceLightOnBackground,
    onSurface = PaceLightOnBackground,
    onSurfaceVariant = Color(0xFF444746)
)

@Composable
fun PaceAmigoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentOption: ColorSchemeOption = ColorSchemeOption.PACE,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        createDarkColorScheme(accentOption)
    } else {
        createLightColorScheme(accentOption)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
