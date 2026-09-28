package ch.simibu.pace.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION_ENABLED, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _screenAwake = MutableStateFlow(prefs.getBoolean(KEY_SCREEN_AWAKE, true))
    val screenAwake: StateFlow<Boolean> = _screenAwake.asStateFlow()

    init {
        if (!prefs.getBoolean(KEY_DEFAULTS_V2, false)) {
            prefs.edit()
                .putInt(KEY_QUICK_FOCUS_MIN, 5)
                .putInt(KEY_QUICK_FOCUS_SEC, 0)
                .putInt(KEY_QUICK_BREAK_MIN, 1)
                .putInt(KEY_QUICK_BREAK_SEC, 0)
                .putInt(KEY_QUICK_ITERATIONS, 5)
                .putBoolean(KEY_DEFAULTS_V2, true)
                .apply()
        }
    }

    // Quick starter last used state
    var quickFocusMinutes: Int
        get() = prefs.getInt(KEY_QUICK_FOCUS_MIN, 5)
        set(value) = prefs.edit().putInt(KEY_QUICK_FOCUS_MIN, value).apply()

    var quickFocusSeconds: Int
        get() = prefs.getInt(KEY_QUICK_FOCUS_SEC, 0)
        set(value) = prefs.edit().putInt(KEY_QUICK_FOCUS_SEC, value).apply()

    var quickBreakMinutes: Int
        get() = prefs.getInt(KEY_QUICK_BREAK_MIN, 1)
        set(value) = prefs.edit().putInt(KEY_QUICK_BREAK_MIN, value).apply()

    var quickBreakSeconds: Int
        get() = prefs.getInt(KEY_QUICK_BREAK_SEC, 0)
        set(value) = prefs.edit().putInt(KEY_QUICK_BREAK_SEC, value).apply()

    var quickIterations: Int
        get() = prefs.getInt(KEY_QUICK_ITERATIONS, 5)
        set(value) = prefs.edit().putInt(KEY_QUICK_ITERATIONS, value).apply()

    var quickWarmupEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUICK_WARMUP, false)
        set(value) = prefs.edit().putBoolean(KEY_QUICK_WARMUP, value).apply()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun setScreenAwake(awake: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_AWAKE, awake).apply()
        _screenAwake.value = awake
    }

    companion object {
        private const val PREFS_NAME = "pace_amigo_settings"
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SCREEN_AWAKE = "screen_awake"

        private const val KEY_QUICK_FOCUS_MIN = "quick_focus_min"
        private const val KEY_QUICK_FOCUS_SEC = "quick_focus_sec"
        private const val KEY_QUICK_BREAK_MIN = "quick_break_min"
        private const val KEY_QUICK_BREAK_SEC = "quick_break_sec"
        private const val KEY_QUICK_ITERATIONS = "quick_iterations"
        private const val KEY_QUICK_WARMUP = "quick_warmup"
        private const val KEY_DEFAULTS_V2 = "defaults_v2_applied"
    }
}
