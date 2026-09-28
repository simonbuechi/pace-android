package ch.simibu.pace.model

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import ch.simibu.pace.R

enum class SoundScheme(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:RawRes val rawResId: Int?
) {
    BEEP("beep", R.string.sound_beep, R.raw.beep),
    BELL("bell", R.string.sound_bell, R.raw.temple_bell),
    PULSE("pulse", R.string.sound_pulse, R.raw.digital_pulse),
    BOWL("bowl", R.string.sound_bowl, R.raw.singing_bowl),
    CHIME("chime", R.string.sound_chime, R.raw.gentle_chime),
    GONG("gong", R.string.sound_gong, R.raw.zen_gong),
    NONE("none", R.string.sound_none, null);

    companion object {
        fun fromId(id: String?): SoundScheme =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BEEP
    }
}
