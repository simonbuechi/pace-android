package ch.simibu.pace.model

import androidx.annotation.StringRes
import ch.simibu.pace.R

enum class TimerPhase(@param:StringRes val titleRes: Int) {
    WARMUP(R.string.timer_phase_warmup),
    FOCUS(R.string.timer_phase_focus),
    BREAK(R.string.timer_phase_break),
    COMPLETED(R.string.timer_phase_complete);

    val isResting: Boolean
        get() = this == BREAK || this == WARMUP

    val isWorking: Boolean
        get() = this == FOCUS
}
