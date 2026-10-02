package ch.simibu.pace

import androidx.compose.ui.graphics.Color
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.ui.screens.getTimerBackgroundGradient
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

        // Must be cool metallic gunmetal / titanium grey
        assertEquals(Color(0xFF343842), gradient.topColor)
        assertEquals(Color(0xFF22252C), gradient.midColor)
        assertEquals(Color(0xFF131518), gradient.bottomColor)
    }

    @Test
    fun testBreakPhaseReturnsFrostedPlatinumSilverInLightTheme() {
        val gradient = getTimerBackgroundGradient(
            phase = TimerPhase.BREAK,
            colorScheme = ColorSchemeOption.CORAL,
            isDark = false
        )

        // Must be frosted platinum / sterling silver
        assertEquals(Color(0xFFF9FAFC), gradient.topColor)
        assertEquals(Color(0xFFE5E9F1), gradient.midColor)
        assertEquals(Color(0xFFD7DCE5), gradient.bottomColor)
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

        // Light theme should produce bright, pastel tones with high RGB values (> 0.7f)
        assertTrue("Ocean top color blue must be prominent in light theme", oceanLight.topColor.blue > 0.8f)
        assertTrue("Ocean top color must be light (> 0.7f)", oceanLight.topColor.red > 0.7f)
    }
}
