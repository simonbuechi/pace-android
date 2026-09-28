package ch.simibu.pace.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import ch.simibu.pace.R
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry

enum class ColorSchemeOption(
    val id: String,
    @param:StringRes val titleRes: Int,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    PACE(
        id = "pace",
        titleRes = R.string.color_pace,
        primaryColor = PaceMagenta,
        secondaryColor = PaceRaspberry
    ),
    CORAL(
        id = "coral",
        titleRes = R.string.color_coral,
        primaryColor = Color(0xFFFF5722),
        secondaryColor = Color(0xFFFF8A65)
    ),
    EMERALD(
        id = "emerald",
        titleRes = R.string.color_emerald,
        primaryColor = Color(0xFF00B074),
        secondaryColor = Color(0xFF69F0AE)
    ),
    VIOLET(
        id = "violet",
        titleRes = R.string.color_violet,
        primaryColor = Color(0xFF7C4DFF),
        secondaryColor = Color(0xFFB388FF)
    ),
    AMBER(
        id = "amber",
        titleRes = R.string.color_amber,
        primaryColor = Color(0xFFFF9100),
        secondaryColor = Color(0xFFFFD180)
    ),
    OCEAN(
        id = "ocean",
        titleRes = R.string.color_ocean,
        primaryColor = Color(0xFF0288D1),
        secondaryColor = Color(0xFF40C4FF)
    );

    companion object {
        fun fromId(id: String?): ColorSchemeOption =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PACE
    }
}
