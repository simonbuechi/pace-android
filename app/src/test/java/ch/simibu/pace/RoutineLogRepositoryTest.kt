package ch.simibu.pace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ch.simibu.pace.data.RoutineLogRepository
import ch.simibu.pace.engine.TimerEngine
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.RoutineLogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoutineLogRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: RoutineLogRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("pace_routine_logs", Context.MODE_PRIVATE).edit().clear().commit()
        repository = RoutineLogRepository(context)
    }

    @Test
    fun testEmptyLogsOnInit() {
        val logs = repository.logs.value
        assertTrue("Initially logs should be empty", logs.isEmpty())
    }

    @Test
    fun testAddLogAndPersistence() {
        val entry = RoutineLogEntry(
            id = "log_1",
            routineId = "routine_a",
            routineName = "Tabata HIIT",
            timestamp = 1000L,
            durationSeconds = 250,
            roundsCompleted = 8
        )
        repository.addLog(entry)

        assertEquals(1, repository.logs.value.size)
        assertEquals("log_1", repository.logs.value[0].id)

        // New repo instance to test persistence
        val reloadedRepo = RoutineLogRepository(context)
        assertEquals(1, reloadedRepo.logs.value.size)
        assertEquals("routine_a", reloadedRepo.logs.value[0].routineId)
    }

    @Test
    fun testFilterAndClearLogsForRoutine() {
        val entry1 = RoutineLogEntry(routineId = "r1", routineName = "R1", durationSeconds = 100, roundsCompleted = 4)
        val entry2 = RoutineLogEntry(routineId = "r2", routineName = "R2", durationSeconds = 200, roundsCompleted = 5)
        val entry3 = RoutineLogEntry(routineId = "r1", routineName = "R1", durationSeconds = 120, roundsCompleted = 4)

        repository.addLog(entry1)
        repository.addLog(entry2)
        repository.addLog(entry3)

        assertEquals(2, repository.getLogsForRoutine("r1").size)
        assertEquals(1, repository.getLogsForRoutine("r2").size)

        repository.clearLogsForRoutine("r1")
        assertTrue(repository.getLogsForRoutine("r1").isEmpty())
        assertEquals(1, repository.getLogsForRoutine("r2").size)
    }

    @Test
    fun testTimerEngineDoesNotLogOnStopOrCancel() {
        val engine = TimerEngine(routineLogRepository = repository)
        val routine = Routine(
            id = "routine_test",
            name = "Test Routine",
            focusMinutes = 0,
            focusSeconds = 5,
            breakMinutes = 0,
            breakSeconds = 5,
            iterations = 2
        )

        engine.startRoutine(routine)
        repeat(3) { engine.performTick() }

        // User stops / cancels session
        engine.stop()

        assertTrue("Cancelled routine should not produce a log entry", repository.logs.value.isEmpty())
    }

    @Test
    fun testTimerEngineLogsOnSuccessfulCompletion() {
        val engine = TimerEngine(routineLogRepository = repository)
        val routine = Routine(
            id = "routine_full",
            name = "Complete Routine",
            focusMinutes = 0,
            focusSeconds = 2,
            breakMinutes = 0,
            breakSeconds = 1,
            iterations = 2,
            warmupSeconds = 1,
            cooldownSeconds = 1
        )

        engine.startRoutine(routine)

        // 1s warmup
        engine.performTick()
        // Round 1: 2s focus
        engine.performTick()
        engine.performTick()
        // Round 1: 1s break
        engine.performTick()
        // Round 2: 2s focus
        engine.performTick()
        engine.performTick()
        // Cooldown: 1s
        engine.performTick()

        // Session completes!
        val logs = repository.getLogsForRoutine("routine_full")
        assertEquals(1, logs.size)
        val log = logs[0]
        assertEquals("routine_full", log.routineId)
        assertEquals("Complete Routine", log.routineName)
        assertEquals(2, log.roundsCompleted)
        assertEquals(routine.totalDurationSeconds, log.durationSeconds)
    }

    @Test
    fun testQuickTimerDoesNotLogAsRoutine() {
        val engine = TimerEngine(routineLogRepository = repository)
        engine.startQuickTimer(focusSec = 2, breakSec = 1, rounds = 1)
        engine.performTick()
        engine.performTick()

        assertTrue("Quick timers should not log as routine logs", repository.logs.value.isEmpty())
    }
}
