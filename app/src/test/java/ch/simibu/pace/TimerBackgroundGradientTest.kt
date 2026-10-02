package ch.simibu.pace

import androidx.compose.ui.graphics.Color
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.ui.components.getTimerBackgroundGradient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerBackgroundGradientTest {

    @Test
    fun testBreakPhaseReturnsMetallicSilverInDarkTheme() {
        val gradient = getTimerBackgroundGradient(
            phase = TimerPhase.BREAK,
            colorScheme = ColorSchemeOption.EMERALD,
            isDark = true
        )

        // Must be cool metallic gunmetal / titanium grey with strong presence
        assertEquals(Color(0xFF424754), gradient.topColor)
        assertEquals(Color(0xFF2B2F38), gradient.midColor)
        assertEquals(Color(0xFF16181D), gradient.bottomColor)
    }

    @Test
    fun testBreakPhaseReturnsFrostedPlatinumSilverInLightTheme() {
        val gradient = getTimerBackgroundGradient(
            phase = TimerPhase.BREAK,
            colorScheme = ColorSchemeOption.CORAL,
            isDark = false
        )

        // Must be frosted platinum / sterling silver with crisp contrast
        assertEquals(Color(0xFFE4E8F0), gradient.topColor)
        assertEquals(Color(0xFFD0D6E2), gradient.midColor)
        assertEquals(Color(0xFFB8C0D0), gradient.bottomColor)
    }

    @Test
    fun testFocusPhaseUsesRoutineSelectedColor() {
        val emeraldDark = getTimerBackgroundGradient(
            phase = TimerPhase.FOCUS,
            colorScheme = ColorSchemeOption.EMERALD,
            isDark = true
        )
        val coralDark = getTimerBackgroundGradient(
            phase = TimerPhase.FOCUS,
            colorScheme = ColorSchemeOption.CORAL,
            isDark = true
        )

        // Different routines must produce distinct gradient palettes
        assertNotEquals(emeraldDark.topColor, coralDark.topColor)
        assertNotEquals(emeraldDark.midColor, coralDark.midColor)

        // Muted dark theme contrast verification: colors should have low luminance (dark jewel tone)
        // Red channel of Emerald is low, green is dominant
        assertTrue(emeraldDark.topColor.green > emeraldDark.topColor.red)
        // Red channel of Coral is dominant
        assertTrue(coralDark.topColor.red > coralDark.topColor.green)
    }

    @Test
    fun testFocusPhaseLightThemeProducesLuminousPastel() {
        val oceanLight = getTimerBackgroundGradient(
            phase = TimerPhase.FOCUS,
            colorScheme = ColorSchemeOption.OCEAN,
            isDark = false
        )

        // Light theme should produce strong, vibrant tones with high RGB values (> 0.45f)
        assertTrue("Ocean top color blue must be prominent in light theme", oceanLight.topColor.blue > 0.8f)
        assertTrue("Ocean top color must remain comfortably light (> 0.45f)", oceanLight.topColor.red > 0.45f)
    }
}
