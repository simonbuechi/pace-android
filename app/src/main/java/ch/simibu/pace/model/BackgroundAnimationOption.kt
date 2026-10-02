package ch.simibu.pace.model

import androidx.annotation.StringRes
import ch.simibu.pace.R

enum class BackgroundAnimationOption(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int
) {
    APP_DEFAULT(
        id = "app_default",
        titleRes = R.string.bg_anim_app_default,
        descRes = R.string.bg_anim_app_default_desc
    ),
    BREATHING_AURA(
        id = "breathing_aura",
        titleRes = R.string.bg_anim_breathing_aura,
        descRes = R.string.bg_anim_breathing_aura_desc
    ),
    AURORA_FLOW(
        id = "aurora_flow",
        titleRes = R.string.bg_anim_aurora_flow,
        descRes = R.string.bg_anim_aurora_flow_desc
    ),
    HORIZON_GLOW(
        id = "horizon_glow",
        titleRes = R.string.bg_anim_horizon_glow,
        descRes = R.string.bg_anim_horizon_glow_desc
    ),
    METALLIC_SHEEN(
        id = "metallic_sheen",
        titleRes = R.string.bg_anim_metallic_sheen,
        descRes = R.string.bg_anim_metallic_sheen_desc
    ),
    SENSOR_PARALLAX(
        id = "sensor_parallax",
        titleRes = R.string.bg_anim_sensor_parallax,
        descRes = R.string.bg_anim_sensor_parallax_desc
    ),
    NONE(
        id = "none",
        titleRes = R.string.bg_anim_none,
        descRes = R.string.bg_anim_none_desc
    );

    companion object {
        val GLOBAL_OPTIONS = listOf(
            BREATHING_AURA,
            AURORA_FLOW,
            HORIZON_GLOW,
            METALLIC_SHEEN,
            SENSOR_PARALLAX,
            NONE
        )

        val ROUTINE_OPTIONS = listOf(
            APP_DEFAULT,
            BREATHING_AURA,
            AURORA_FLOW,
            HORIZON_GLOW,
            METALLIC_SHEEN,
            SENSOR_PARALLAX,
            NONE
        )

        fun fromId(id: String?): BackgroundAnimationOption =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: APP_DEFAULT
    }
}
