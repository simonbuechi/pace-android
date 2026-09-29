package ch.simibu.pace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ch.simibu.pace.data.SettingsRepository
import ch.simibu.pace.model.SoundScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("pace_amigo_settings", Context.MODE_PRIVATE).edit().clear().commit()
        repository = SettingsRepository(context)
    }

    @Test
    fun testDefaultSettings() {
        // Quick Start defaults: 5min focus, 1min break, 5 rounds
        assertEquals(5, repository.quickFocusMinutes)
        assertEquals(0, repository.quickFocusSeconds)
        assertEquals(1, repository.quickBreakMinutes)
        assertEquals(0, repository.quickBreakSeconds)
        assertEquals(5, repository.quickIterations)

        // General defaults
        assertTrue(repository.soundEnabled.value)
        assertTrue(repository.vibrationEnabled.value)
        assertTrue(repository.screenAwake.value)
        assertEquals(1, repository.focusSoundRepeats.value)
        assertEquals(1, repository.breakSoundRepeats.value)
    }

    @Test
    fun testSoundSelectionAndRepeats() {
        repository.setFocusSoundScheme(SoundScheme.GONG)
        assertEquals(SoundScheme.GONG, repository.focusSoundScheme.value)

        repository.setBreakSoundScheme(SoundScheme.CHIME)
        assertEquals(SoundScheme.CHIME, repository.breakSoundScheme.value)

        // Test repeat setting and clamping (1 to 5)
        repository.setFocusSoundRepeats(3)
        assertEquals(3, repository.focusSoundRepeats.value)

        // Clamp below 1
        repository.setFocusSoundRepeats(0)
        assertEquals(1, repository.focusSoundRepeats.value)

        // Clamp above 5
        repository.setFocusSoundRepeats(10)
        assertEquals(5, repository.focusSoundRepeats.value)
    }

    @Test
    fun testScreenAwakeAndVibrationToggles() {
        repository.setScreenAwake(false)
        assertFalse(repository.screenAwake.value)

        repository.setVibrationEnabled(false)
        assertFalse(repository.vibrationEnabled.value)

        repository.setSoundEnabled(false)
        assertFalse(repository.soundEnabled.value)
    }

    @Test
    fun testSettingsPersistenceAcrossInstances() {
        repository.quickFocusMinutes = 10
        repository.quickBreakMinutes = 2
        repository.quickIterations = 8
        repository.setFocusSoundScheme(SoundScheme.PULSE)
        repository.setFocusSoundRepeats(4)
        repository.setScreenAwake(false)

        val secondRepo = SettingsRepository(context)
        assertEquals(10, secondRepo.quickFocusMinutes)
        assertEquals(2, secondRepo.quickBreakMinutes)
        assertEquals(8, secondRepo.quickIterations)
        assertEquals(SoundScheme.PULSE, secondRepo.focusSoundScheme.value)
        assertEquals(4, secondRepo.focusSoundRepeats.value)
        assertFalse(secondRepo.screenAwake.value)
    }
}
