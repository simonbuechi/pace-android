package ch.simibu.pace

import ch.simibu.pace.engine.TimerEngine
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerEngineTest {

    @Test
    fun testQuickTimerInitialization() {
        val engine = TimerEngine()
        engine.startQuickTimer(focusSec = 30, breakSec = 10, rounds = 5, warmupSec = 0)

        val state = engine.state.value
        assertEquals(TimerPhase.FOCUS, state.phase)
        assertEquals(1, state.currentRound)
        assertEquals(5, state.totalRounds)
        assertEquals(30, state.remainingSecondsInPhase)
        assertEquals(30, state.totalSecondsInPhase)
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isCompleted)
    }

    @Test
    fun testWarmupPhaseTransition() {
        val engine = TimerEngine()
        engine.startQuickTimer(focusSec = 20, breakSec = 10, rounds = 3, warmupSec = 5)

        assertEquals(TimerPhase.WARMUP, engine.state.value.phase)
        assertEquals(5, engine.state.value.remainingSecondsInPhase)

        // Tick 5 times to complete warmup
        repeat(5) { engine.performTick() }

        val afterWarmupState = engine.state.value
        assertEquals(TimerPhase.FOCUS, afterWarmupState.phase)
        assertEquals(1, afterWarmupState.currentRound)
        assertEquals(20, afterWarmupState.remainingSecondsInPhase)
    }

    @Test
    fun testFocusToBreakAndNextRound() {
        val engine = TimerEngine()
        engine.startQuickTimer(focusSec = 5, breakSec = 3, rounds = 2, warmupSec = 0)

        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertEquals(1, engine.state.value.currentRound)

        // 5 ticks: Focus should end -> Break begins
        repeat(5) { engine.performTick() }
        assertEquals(TimerPhase.BREAK, engine.state.value.phase)
        assertEquals(1, engine.state.value.currentRound)
        assertEquals(3, engine.state.value.remainingSecondsInPhase)

        // 3 ticks: Break should end -> Focus Round 2 begins
        repeat(3) { engine.performTick() }
        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertEquals(2, engine.state.value.currentRound)
        assertEquals(5, engine.state.value.remainingSecondsInPhase)

        // 5 ticks: Final Focus ends -> Session Completed!
        repeat(5) { engine.performTick() }
        assertEquals(TimerPhase.COMPLETED, engine.state.value.phase)
        assertTrue(engine.state.value.isCompleted)
        assertFalse(engine.state.value.isRunning)
    }

    @Test
    fun testPauseResume() {
        val engine = TimerEngine()
        engine.startQuickTimer(focusSec = 10, breakSec = 5, rounds = 2)

        repeat(2) { engine.performTick() }
        assertEquals(8, engine.state.value.remainingSecondsInPhase)

        engine.pause()
        assertTrue(engine.state.value.isPaused)

        // While paused, time should not decrease on tick
        repeat(3) { engine.performTick() }
        assertEquals(8, engine.state.value.remainingSecondsInPhase)

        engine.resume()
        assertFalse(engine.state.value.isPaused)

        repeat(2) { engine.performTick() }
        assertEquals(6, engine.state.value.remainingSecondsInPhase)
    }

    @Test
    fun testSkipPhase() {
        val engine = TimerEngine()
        engine.startQuickTimer(focusSec = 45, breakSec = 15, rounds = 3)

        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertEquals(1, engine.state.value.currentRound)

        engine.skipPhase()
        assertEquals(TimerPhase.BREAK, engine.state.value.phase)
        assertEquals(1, engine.state.value.currentRound)

        engine.skipPhase()
        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertEquals(2, engine.state.value.currentRound)
    }

    @Test
    fun testRoutineDurationCalculation() {
        val routine = Routine(
            id = "test",
            name = "Test",
            focusMinutes = 1,
            focusSeconds = 30, // 90s
            breakMinutes = 0,
            breakSeconds = 30, // 30s
            iterations = 4,    // 4 rounds * 120s = 480s
            warmupSeconds = 10 // + 10s = 490s
        )

        assertEquals(90, routine.totalFocusSeconds)
        assertEquals(30, routine.totalBreakSeconds)
        assertEquals(490, routine.totalDurationSeconds)
    }
}
