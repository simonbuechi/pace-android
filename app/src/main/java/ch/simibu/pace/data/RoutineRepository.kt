package ch.simibu.pace.data

import android.content.Context
import ch.simibu.pace.model.Routine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoutineRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines.asStateFlow()

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        val savedJson = prefs.getString(KEY_ROUTINES, null)
        val loadedList = if (!savedJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<Routine>>(savedJson)
            } catch (e: Exception) {
                Routine.createDefaultPresets()
            }
        } else {
            val defaults = Routine.createDefaultPresets()
            saveRoutinesInternal(defaults)
            defaults
        }
        _routines.value = loadedList
    }

    fun saveRoutine(routine: Routine) {
        val current = _routines.value.toMutableList()
        val index = current.indexOfFirst { it.id == routine.id }
        if (index != -1) {
            current[index] = routine
        } else {
            current.add(0, routine)
        }
        saveRoutinesInternal(current)
    }

    fun deleteRoutine(routineId: String) {
        val filtered = _routines.value.filterNot { it.id == routineId }
        saveRoutinesInternal(filtered)
    }

    fun getRoutine(id: String): Routine? {
        return _routines.value.firstOrNull { it.id == id }
    }

    private fun saveRoutinesInternal(list: List<Routine>) {
        try {
            val encoded = json.encodeToString(list)
            prefs.edit().putString(KEY_ROUTINES, encoded).apply()
            _routines.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PREFS_NAME = "pace_amigo_routines"
        private const val KEY_ROUTINES = "saved_routines_json"
    }
}
