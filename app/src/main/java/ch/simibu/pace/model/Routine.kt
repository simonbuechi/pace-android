package ch.simibu.pace.model

import kotlinx.serialization.Serializable

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val category: String = "",
    val focusMinutes: Int,
    val focusSeconds: Int,
    val breakMinutes: Int,
    val breakSeconds: Int,
    val iterations: Int = 8,
    val warmupMinutes: Int = 0,
    val warmupSeconds: Int = 0,
    val cooldownMinutes: Int = 0,
    val cooldownSeconds: Int = 0,
    val soundSchemeId: String = SoundScheme.BEEP.id,
    val colorSchemeId: String = ColorSchemeOption.PACE.id,
    val countdownSignalEnabled: Boolean = true,
    val countdownSignalSeconds: Int = 3
) {
    val totalFocusSeconds: Int
        get() = (focusMinutes * 60) + focusSeconds

    val totalBreakSeconds: Int
        get() = (breakMinutes * 60) + breakSeconds

    val totalWarmupSeconds: Int
        get() = (warmupMinutes * 60) + warmupSeconds

    val totalCooldownSeconds: Int
        get() = (cooldownMinutes * 60) + cooldownSeconds

    val totalDurationSeconds: Int
        get() = totalWarmupSeconds + totalCooldownSeconds + ((totalFocusSeconds + totalBreakSeconds) * iterations)

    val soundScheme: SoundScheme
        get() = SoundScheme.fromId(soundSchemeId)

    val colorScheme: ColorSchemeOption
        get() = ColorSchemeOption.fromId(colorSchemeId)

    companion object {
        val COUNTDOWN_SIGNAL_OPTIONS = listOf(3, 5, 10, 20, 30, 60)

        fun createDefaultPresets(): List<Routine> = listOf(
            Routine(
                id = "preset_tabata",
                name = "Tabata HIIT",
                focusMinutes = 0,
                focusSeconds = 20,
                breakMinutes = 0,
                breakSeconds = 10,
                iterations = 8,
                warmupMinutes = 0,
                warmupSeconds = 10,
                cooldownMinutes = 0,
                cooldownSeconds = 30,
                soundSchemeId = SoundScheme.BELL.id,
                colorSchemeId = ColorSchemeOption.PACE.id
            ),
            Routine(
                id = "preset_pomodoro",
                name = "Classic Pomodoro",
                focusMinutes = 25,
                focusSeconds = 0,
                breakMinutes = 5,
                breakSeconds = 0,
                iterations = 4,
                warmupMinutes = 0,
                warmupSeconds = 0,
                cooldownMinutes = 0,
                cooldownSeconds = 0,
                soundSchemeId = SoundScheme.CHIME.id,
                colorSchemeId = ColorSchemeOption.EMERALD.id
            ),
            Routine(
                id = "preset_boxing",
                name = "Boxing Rounds",
                focusMinutes = 3,
                focusSeconds = 0,
                breakMinutes = 1,
                breakSeconds = 0,
                iterations = 5,
                warmupMinutes = 1,
                warmupSeconds = 0,
                cooldownMinutes = 1,
                cooldownSeconds = 0,
                soundSchemeId = SoundScheme.GONG.id,
                colorSchemeId = ColorSchemeOption.AMBER.id
            ),
            Routine(
                id = "preset_stretch",
                name = "Mobility & Stretch",
                focusMinutes = 0,
                focusSeconds = 45,
                breakMinutes = 0,
                breakSeconds = 15,
                iterations = 10,
                warmupMinutes = 0,
                warmupSeconds = 15,
                cooldownMinutes = 0,
                cooldownSeconds = 30,
                soundSchemeId = SoundScheme.BOWL.id,
                colorSchemeId = ColorSchemeOption.OCEAN.id
            )
        )
    }
}
