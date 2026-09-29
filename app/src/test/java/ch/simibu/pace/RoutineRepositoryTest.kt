package ch.simibu.pace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ch.simibu.pace.data.RoutineRepository
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.SoundScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoutineRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: RoutineRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs between tests
        context.getSharedPreferences("pace_amigo_routines", Context.MODE_PRIVATE).edit().clear().commit()
        repository = RoutineRepository(context)
    }

    @Test
    fun testDefaultRoutinesLoadedOnInit() {
        val routines = repository.routines.value
        assertTrue("Repository should have default routines", routines.isNotEmpty())
        assertEquals(4, routines.size)

        val tabata = repository.getRoutine("preset_tabata")
        assertNotNull("Tabata routine should exist", tabata)
        assertEquals(8, tabata?.iterations)
        assertEquals(20, tabata?.focusSeconds)
        assertEquals(10, tabata?.breakSeconds)

        val pomodoro = repository.getRoutine("preset_pomodoro")
        assertNotNull("Pomodoro routine should exist", pomodoro)
        assertEquals(25, pomodoro?.focusMinutes)
        assertEquals(5, pomodoro?.breakMinutes)
    }

    @Test
    fun testSaveNewRoutine() {
        val newRoutine = Routine(
            id = "custom_test_1",
            name = "Sprint Intervals",
            focusMinutes = 0,
            focusSeconds = 45,
            breakMinutes = 0,
            breakSeconds = 15,
            iterations = 6,
            warmupMinutes = 1,
            warmupSeconds = 0,
            cooldownMinutes = 2,
            cooldownSeconds = 0,
            soundSchemeId = SoundScheme.BELL.id,
            colorSchemeId = ColorSchemeOption.PACE.id
        )

        repository.saveRoutine(newRoutine)

        val retrieved = repository.getRoutine("custom_test_1")
        assertNotNull(retrieved)
        assertEquals("Sprint Intervals", retrieved?.name)
        assertEquals(45, retrieved?.focusSeconds)
        assertEquals(6, retrieved?.iterations)
        assertEquals(60, retrieved?.warmupMinutes?.times(60)?.plus(retrieved.warmupSeconds))
    }

    @Test
    fun testUpdateExistingRoutine() {
        val tabata = repository.getRoutine("preset_tabata")
        assertNotNull(tabata)

        val modified = tabata!!.copy(iterations = 12, name = "Extended Tabata")
        repository.saveRoutine(modified)

        val updated = repository.getRoutine("preset_tabata")
        assertEquals(12, updated?.iterations)
        assertEquals("Extended Tabata", updated?.name)
    }

    @Test
    fun testDeleteRoutine() {
        assertNotNull(repository.getRoutine("preset_tabata"))

        repository.deleteRoutine("preset_tabata")
        assertNull(repository.getRoutine("preset_tabata"))
        assertEquals(3, repository.routines.value.size)
    }

    @Test
    fun testPersistenceAcrossInstances() {
        val custom = Routine(
            id = "persist_test",
            name = "Persistent Routine",
            focusMinutes = 3,
            focusSeconds = 0,
            breakMinutes = 1,
            breakSeconds = 0,
            iterations = 3
        )
        repository.saveRoutine(custom)

        // Instantiate a second repository with the same context
        val secondRepo = RoutineRepository(context)
        val loaded = secondRepo.getRoutine("persist_test")
        assertNotNull("Routine should persist across repository instances", loaded)
        assertEquals("Persistent Routine", loaded?.name)
        assertEquals(3, loaded?.iterations)
    }
}
