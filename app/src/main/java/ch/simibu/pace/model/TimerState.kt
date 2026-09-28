package ch.simibu.pace.model

data class TimerState(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val currentRound: Int = 1,
    val totalRounds: Int = 8,
    val remainingSecondsInPhase: Int = 0,
    val totalSecondsInPhase: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val routineName: String = "",
    val soundScheme: SoundScheme = SoundScheme.BEEP,
    val colorScheme: ColorSchemeOption = ColorSchemeOption.PACE
) {
    val progress: Float
        get() = if (totalSecondsInPhase > 0) {
            1f - (remainingSecondsInPhase.toFloat() / totalSecondsInPhase.toFloat())
        } else 0f

    val formattedRemainingTime: String
        get() {
            val mins = remainingSecondsInPhase / 60
            val secs = remainingSecondsInPhase % 60
            return "%02d:%02d".format(mins, secs)
        }
}
