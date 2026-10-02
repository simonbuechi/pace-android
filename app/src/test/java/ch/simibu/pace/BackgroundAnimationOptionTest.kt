package ch.simibu.pace

import ch.simibu.pace.engine.TimerEngine
import ch.simibu.pace.model.BackgroundAnimationOption
import ch.simibu.pace.model.Routine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundAnimationOptionTest {

    @Test
    fun testFromIdResolvesKnownOptions() {
        assertEquals(BackgroundAnimationOption.BREATHING_AURA, BackgroundAnimationOption.fromId("breathing_aura"))
        assertEquals(BackgroundAnimationOption.AURORA_FLOW, BackgroundAnimationOption.fromId("AURORA_FLOW"))
        assertEquals(BackgroundAnimationOption.HORIZON_GLOW, BackgroundAnimationOption.fromId("horizon_glow"))
        assertEquals(BackgroundAnimationOption.METALLIC_SHEEN, BackgroundAnimationOption.fromId("metallic_sheen"))
        assertEquals(BackgroundAnimationOption.SENSOR_PARALLAX, BackgroundAnimationOption.fromId("sensor_parallax"))
        assertEquals(BackgroundAnimationOption.NONE, BackgroundAnimationOption.fromId("none"))
        assertEquals(BackgroundAnimationOption.APP_DEFAULT, BackgroundAnimationOption.fromId("app_default"))
    }

    @Test
    fun testFromIdFallsBackToAppDefaultForNullOrInvalid() {
        assertEquals(BackgroundAnimationOption.APP_DEFAULT, BackgroundAnimationOption.fromId(null))
        assertEquals(BackgroundAnimationOption.APP_DEFAULT, BackgroundAnimationOption.fromId("unknown_mode"))
    }

    @Test
    fun testGlobalOptionsDoesNotContainAppDefault() {
        assertFalse(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.APP_DEFAULT))
        assertEquals(6, BackgroundAnimationOption.GLOBAL_OPTIONS.size)
        assertTrue(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.BREATHING_AURA))
        assertTrue(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.AURORA_FLOW))
        assertTrue(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.HORIZON_GLOW))
        assertTrue(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.METALLIC_SHEEN))
        assertTrue(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.SENSOR_PARALLAX))
        assertTrue(BackgroundAnimationOption.GLOBAL_OPTIONS.contains(BackgroundAnimationOption.NONE))
    }

    @Test
    fun testRoutineOptionsContainsAllOptions() {
        assertEquals(7, BackgroundAnimationOption.ROUTINE_OPTIONS.size)
        assertTrue(BackgroundAnimationOption.ROUTINE_OPTIONS.contains(BackgroundAnimationOption.APP_DEFAULT))
    }

    @Test
    fun testRoutineResolvesBackgroundAnimationFromId() {
        val routineDefault = Routine(
            id = "1",
            name = "Test 1",
            focusMinutes = 0,
            focusSeconds = 30,
            breakMinutes = 0,
            breakSeconds = 15
        )
        assertEquals(BackgroundAnimationOption.APP_DEFAULT, routineDefault.backgroundAnimation)

        val routineMetallic = Routine(
            id = "2",
            name = "Test 2",
            focusMinutes = 0,
            focusSeconds = 30,
            breakMinutes = 0,
            breakSeconds = 15,
            backgroundAnimationId = "metallic_sheen"
        )
        assertEquals(BackgroundAnimationOption.METALLIC_SHEEN, routineMetallic.backgroundAnimation)
    }

    @Test
    fun testTimerEngineInitializesWithBackgroundAnimation() {
        val engine = TimerEngine()

        // Quick timer with Aurora Flow
        engine.startQuickTimer(
            focusSec = 30,
            breakSec = 10,
            rounds = 2,
            backgroundAnimation = BackgroundAnimationOption.AURORA_FLOW
        )
        assertEquals(BackgroundAnimationOption.AURORA_FLOW, engine.state.value.backgroundAnimation)

        // Routine with Horizon Glow
        val routine = Routine(
            id = "3",
            name = "Glow Routine",
            focusMinutes = 0,
            focusSeconds = 30,
            breakMinutes = 0,
            breakSeconds = 15,
            backgroundAnimationId = "horizon_glow"
        )
        engine.startRoutine(routine)
        assertEquals(BackgroundAnimationOption.HORIZON_GLOW, engine.state.value.backgroundAnimation)
    }
}
