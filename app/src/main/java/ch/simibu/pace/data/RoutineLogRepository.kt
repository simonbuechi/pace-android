package ch.simibu.pace.data

import android.content.Context
import ch.simibu.pace.model.RoutineLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoutineLogRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _logs = MutableStateFlow<List<RoutineLogEntry>>(emptyList())
    val logs: StateFlow<List<RoutineLogEntry>> = _logs.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        val savedJson = prefs.getString(KEY_LOGS, null)
        val loadedList = if (!savedJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<RoutineLogEntry>>(savedJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _logs.value = loadedList
    }

    fun addLog(entry: RoutineLogEntry) {
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        saveLogsInternal(current)
    }

    fun getLogsForRoutine(routineId: String): List<RoutineLogEntry> {
        return _logs.value.filter { it.routineId == routineId }
    }

    fun clearLogsForRoutine(routineId: String) {
        val filtered = _logs.value.filterNot { it.routineId == routineId }
        saveLogsInternal(filtered)
    }

    fun clearAllLogs() {
        saveLogsInternal(emptyList())
    }

    private fun saveLogsInternal(list: List<RoutineLogEntry>) {
        try {
            val encoded = json.encodeToString(list)
            prefs.edit().putString(KEY_LOGS, encoded).apply()
            _logs.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PREFS_NAME = "pace_routine_logs"
        private const val KEY_LOGS = "saved_routine_logs_json"
    }
}
