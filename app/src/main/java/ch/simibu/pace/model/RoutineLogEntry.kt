package ch.simibu.pace.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RoutineLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val routineId: String,
    val routineName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val roundsCompleted: Int = 0
)
