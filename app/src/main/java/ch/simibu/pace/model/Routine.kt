package ch.simibu.pace.model

import kotlinx.serialization.Serializable

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val category: String = "Workout",
    val focusMinutes: Int,
    val focusSeconds: Int,
    val breakMinutes: Int,
    val breakSeconds: Int,
    val iterations: Int = 8,
    val warmupSeconds: Int = 0,
    val soundSchemeId: String = SoundScheme.BEEP.id,
    val colorSchemeId: String = ColorSchemeOption.CORAL.id
) {
    val totalFocusSeconds: Int
        get() = (focusMinutes * 60) + focusSeconds

    val totalBreakSeconds: Int
        get() = (breakMinutes * 60) + breakSeconds

    val totalDurationSeconds: Int
        get() = warmupSeconds + ((totalFocusSeconds + totalBreakSeconds) * iterations)

    val soundScheme: SoundScheme
        get() = SoundScheme.fromId(soundSchemeId)

    val colorScheme: ColorSchemeOption
        get() = ColorSchemeOption.fromId(colorSchemeId)

    companion object {
        fun createDefaultPresets(): List<Routine> = listOf(
            Routine(
                id = "preset_tabata",
                name = "Tabata HIIT",
                category = "HIIT",
                focusMinutes = 0,
                focusSeconds = 20,
                breakMinutes = 0,
                breakSeconds = 10,
                iterations = 8,
                warmupSeconds = 10,
                soundSchemeId = SoundScheme.BELL.id,
                colorSchemeId = ColorSchemeOption.CORAL.id
            ),
            Routine(
                id = "preset_pomodoro",
                name = "Classic Pomodoro",
                category = "Productivity",
                focusMinutes = 25,
                focusSeconds = 0,
                breakMinutes = 5,
                breakSeconds = 0,
                iterations = 4,
                warmupSeconds = 0,
                soundSchemeId = SoundScheme.CHIME.id,
                colorSchemeId = ColorSchemeOption.EMERALD.id
            ),
            Routine(
                id = "preset_boxing",
                name = "Boxing Rounds",
                category = "Combat",
                focusMinutes = 3,
                focusSeconds = 0,
                breakMinutes = 1,
                breakSeconds = 0,
                iterations = 5,
                warmupSeconds = 10,
                soundSchemeId = SoundScheme.GONG.id,
                colorSchemeId = ColorSchemeOption.AMBER.id
            ),
            Routine(
                id = "preset_stretch",
                name = "Mobility & Stretch",
                category = "Recovery",
                focusMinutes = 0,
                focusSeconds = 45,
                breakMinutes = 0,
                breakSeconds = 15,
                iterations = 10,
                warmupSeconds = 0,
                soundSchemeId = SoundScheme.BOWL.id,
                colorSchemeId = ColorSchemeOption.OCEAN.id
            )
        )
    }
}
