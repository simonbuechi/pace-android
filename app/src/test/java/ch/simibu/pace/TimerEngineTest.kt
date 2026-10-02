package ch.simibu.pace

import ch.simibu.pace.engine.TimerEngine
import ch.simibu.pace.engine.TimerEvent
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.TimerPhase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
    fun testCooldownPhaseTransition() {
        val engine = TimerEngine()
        engine.startQuickTimer(focusSec = 5, breakSec = 2, rounds = 1, warmupSec = 0, cooldownSec = 8)

        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)

        // 5 ticks: round 1 focus finishes -> transitions to COOLDOWN (not directly completed)
        repeat(5) { engine.performTick() }
        assertEquals(TimerPhase.COOLDOWN, engine.state.value.phase)
        assertEquals(8, engine.state.value.remainingSecondsInPhase)
        assertTrue(engine.state.value.isRunning)

        // 8 ticks: cooldown finishes -> COMPLETED
        repeat(8) { engine.performTick() }
        assertEquals(TimerPhase.COMPLETED, engine.state.value.phase)
        assertTrue(engine.state.value.isCompleted)
        assertFalse(engine.state.value.isRunning)
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
            warmupMinutes = 0,
            warmupSeconds = 10, // + 10s = 490s
            cooldownMinutes = 1,
            cooldownSeconds = 0  // + 60s = 550s
        )

        assertEquals(90, routine.totalFocusSeconds)
        assertEquals(30, routine.totalBreakSeconds)
        assertEquals(10, routine.totalWarmupSeconds)
        assertEquals(60, routine.totalCooldownSeconds)
        assertEquals(550, routine.totalDurationSeconds)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testCountdownSignalEmittedWhenEnabled() = runTest {
        val engine = TimerEngine()
        val routine = Routine(
            id = "test_signal",
            name = "Signal Test",
            focusMinutes = 0,
            focusSeconds = 10,
            breakMinutes = 0,
            breakSeconds = 0,
            iterations = 1,
            countdownSignalEnabled = true,
            countdownSignalSeconds = 5
        )
        val collected = mutableListOf<TimerEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            engine.events.collect { collected.add(it) }
        }
        engine.startRoutine(routine)

        // 4 ticks: remaining decreases from 10 down to 6 -> no countdown tick yet
        repeat(4) { engine.performTick() }
        assertTrue(collected.none { it is TimerEvent.CountdownTick })

        // 5th tick: remaining is 5 -> CountdownTick(5) emitted
        engine.performTick()
        val ticks5 = collected.filterIsInstance<TimerEvent.CountdownTick>()
        assertEquals(1, ticks5.size)
        assertEquals(5, ticks5.first().secondsLeft)

        // Remaining 4, 3, 2, 1 -> 4 more ticks
        repeat(4) { engine.performTick() }
        val allTicks = collected.filterIsInstance<TimerEvent.CountdownTick>()
        assertEquals(5, allTicks.size)
        assertEquals(listOf(5, 4, 3, 2, 1), allTicks.map { it.secondsLeft })
        job.cancel()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testCountdownSignalSuppressedWhenDisabled() = runTest {
        val engine = TimerEngine()
        val routine = Routine(
            id = "test_disabled",
            name = "Disabled Test",
            focusMinutes = 0,
            focusSeconds = 6,
            breakMinutes = 0,
            breakSeconds = 0,
            iterations = 1,
            countdownSignalEnabled = false,
            countdownSignalSeconds = 5
        )
        val collected = mutableListOf<TimerEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            engine.events.collect { collected.add(it) }
        }
        engine.startRoutine(routine)

        // Tick down through 5..1
        repeat(5) { engine.performTick() }
        assertTrue(collected.none { it is TimerEvent.CountdownTick })
        job.cancel()
    }
}
