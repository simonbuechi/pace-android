package ch.simibu.pace

import app.cash.turbine.test
import ch.simibu.pace.engine.TimerEngine
import ch.simibu.pace.engine.TimerEvent
import ch.simibu.pace.model.SoundScheme
import ch.simibu.pace.model.TimerPhase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun testStateFlowEmissionsOnTimerStartAndTick() = runTest(testDispatcher) {
        val engine = TimerEngine(scope = testScope, dispatcher = testDispatcher)

        engine.state.test {
            // Initial state (not running)
            val initial = awaitItem()
            assertFalse(initial.isRunning)
            assertEquals(0, initial.remainingSecondsInPhase)

            // Start quick session: 10s focus, 5s break, 1 round
            engine.startQuickTimer(focusSec = 10, breakSec = 5, rounds = 1)
            val startedState = awaitItem()
            assertEquals(TimerPhase.FOCUS, startedState.phase)
            assertEquals(10, startedState.remainingSecondsInPhase)
            assertTrue(startedState.isRunning)

            // Perform a manual tick
            engine.performTick()
            val tickedState = awaitItem()
            assertEquals(9, tickedState.remainingSecondsInPhase)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testEventFlowEmissionsOnPhaseTransitionAndCountdown() = runTest(testDispatcher) {
        val engine = TimerEngine(scope = testScope, dispatcher = testDispatcher)

        engine.events.test {
            // Start routine with 2 rounds: 4s focus, 2s break
            engine.startQuickTimer(
                focusSec = 4,
                breakSec = 2,
                rounds = 2,
                warmupSec = 0,
                focusSound = SoundScheme.BELL,
                focusRepeats = 2,
                breakSound = SoundScheme.GONG,
                breakRepeats = 3
            )

            // 1. Initial start event
            val startEvent = awaitItem()
            assertTrue(startEvent is TimerEvent.PhaseTransition)
            val ptStart = startEvent as TimerEvent.PhaseTransition
            assertEquals(TimerPhase.FOCUS, ptStart.newPhase)
            assertEquals(SoundScheme.BELL, ptStart.scheme)
            assertEquals(2, ptStart.repeats)

            // Tick 1: 4s -> 3s left (triggers 3s countdown tick)
            engine.performTick()
            val tick3 = awaitItem()
            assertTrue(tick3 is TimerEvent.CountdownTick)
            assertEquals(3, (tick3 as TimerEvent.CountdownTick).secondsLeft)

            // Tick 2: 3s -> 2s left (triggers 2s countdown tick)
            engine.performTick()
            val tick2 = awaitItem()
            assertTrue(tick2 is TimerEvent.CountdownTick)
            assertEquals(2, (tick2 as TimerEvent.CountdownTick).secondsLeft)

            // Tick 3: 2s -> 1s left (triggers 1s countdown tick)
            engine.performTick()
            val tick1 = awaitItem()
            assertTrue(tick1 is TimerEvent.CountdownTick)
            assertEquals(1, (tick1 as TimerEvent.CountdownTick).secondsLeft)

            // Tick 4: 1s -> 0s left (transition to BREAK)
            engine.performTick()
            val breakEvent = awaitItem()
            assertTrue(breakEvent is TimerEvent.PhaseTransition)
            val ptBreak = breakEvent as TimerEvent.PhaseTransition
            assertEquals(TimerPhase.BREAK, ptBreak.newPhase)
            assertEquals(SoundScheme.GONG, ptBreak.scheme)
            assertEquals(3, ptBreak.repeats)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
