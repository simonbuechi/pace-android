package ch.simibu.pace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.model.Routine
import ch.simibu.pace.service.PaceTimerService

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import ch.simibu.pace.ui.components.TactileCard
import ch.simibu.pace.ui.components.TactileCircleButton
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.components.TactileSunkenWell
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry

@Composable
fun RoutinesScreen(
    onStartRoutine: (Routine) -> Unit,
    showNewRoutineDialog: Boolean = false,
    onResetShowNewDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val routineRepo = PaceApplication.instance.routineRepository
    val routineLogRepo = PaceApplication.instance.routineLogRepository
    val timerEngine = PaceApplication.instance.timerEngine

    val routines by routineRepo.routines.collectAsState()
    val logs by routineLogRepo.logs.collectAsState()

    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
    var viewingLogRoutine by remember { mutableStateOf<Routine?>(null) }
    var isAddingNew by remember { mutableStateOf(showNewRoutineDialog) }
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }

    Scaffold(
        floatingActionButton = {
            TactilePillButton(
                modifier = Modifier
                    .size(60.dp)
                    .clickable { isAddingNew = true },
                shape = CircleShape,
                elevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PaceBrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.routine_new),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.routines_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_routines),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(routines, key = { it.id }) { routine ->
                        RoutineCard(
                            routine = routine,
                            onPlay = {
                                timerEngine.startRoutine(routine)
                                PaceTimerService.start(context)
                                onStartRoutine(routine)
                            },
                            onViewLog = { viewingLogRoutine = routine },
                            onEdit = { editingRoutine = routine },
                            onDelete = { routineToDelete = routine }
                        )
                    }
                }
            }
        }
    }

    // Routine Log Viewer Dialog
    viewingLogRoutine?.let { routine ->
        val routineLogs = logs.filter { it.routineId == routine.id }
        RoutineLogDialog(
            routine = routine,
            logs = routineLogs,
            onDismiss = { viewingLogRoutine = null },
            onClearLogs = { routineLogRepo.clearLogsForRoutine(routine.id) }
        )
    }

    // Routine Editor Dialog
    if (isAddingNew || editingRoutine != null) {
        RoutineEditorDialog(
            initialRoutine = editingRoutine,
            onDismiss = {
                isAddingNew = false
                editingRoutine = null
                onResetShowNewDialog()
            },
            onSave = { savedRoutine ->
                routineRepo.saveRoutine(savedRoutine)
                isAddingNew = false
                editingRoutine = null
                onResetShowNewDialog()
            }
        )
    }

    // Delete Confirmation Dialog
    routineToDelete?.let { routine ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = { Text(stringResource(R.string.btn_delete)) },
            text = { Text(stringResource(R.string.routine_delete_confirm, routine.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        routineRepo.deleteRoutine(routine.id)
                        routineLogRepo.clearLogsForRoutine(routine.id)
                        routineToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
fun RoutineCard(
    routine: Routine,
    onPlay: () -> Unit,
    onViewLog: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Header Row: Routine Name & Unified Total Duration Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Single unified total duration pill
                TactileSunkenWell(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val totalSec = routine.totalDurationSeconds
                    val mins = totalSec / 60
                    val secs = totalSec % 60
                    val durationText = if (mins > 0 && secs > 0) "${mins}m ${secs}s"
                    else if (mins > 0) "${mins} min"
                    else "${secs} sec"
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Metrics (Clean, elegant, calm typography - zero rainbow badges)
            val focusStr = if (routine.focusMinutes > 0 && routine.focusSeconds > 0) "${routine.focusMinutes}m ${routine.focusSeconds}s"
            else if (routine.focusMinutes > 0) "${routine.focusMinutes}m"
            else "${routine.focusSeconds}s"

            val breakStr = if (routine.breakMinutes > 0 && routine.breakSeconds > 0) "${routine.breakMinutes}m ${routine.breakSeconds}s"
            else if (routine.breakMinutes > 0) "${routine.breakMinutes}m"
            else "${routine.breakSeconds}s"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = focusStr,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.timer_phase_focus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = breakStr,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.timer_phase_break),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "${routine.iterations}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PaceRaspberry
                )
            }

            // Optional Warm-up & Cool-down line (Subtle and quiet)
            if (routine.totalWarmupSeconds > 0 || routine.totalCooldownSeconds > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                val warmupPart = if (routine.totalWarmupSeconds > 0) {
                    val wStr = if (routine.warmupMinutes > 0 && routine.warmupSeconds > 0) "${routine.warmupMinutes}m ${routine.warmupSeconds}s"
                    else if (routine.warmupMinutes > 0) "${routine.warmupMinutes}m"
                    else "${routine.warmupSeconds}s"
                    "+ $wStr " + stringResource(R.string.timer_phase_warmup)
                } else null

                val cooldownPart = if (routine.totalCooldownSeconds > 0) {
                    val cStr = if (routine.cooldownMinutes > 0 && routine.cooldownSeconds > 0) "${routine.cooldownMinutes}m ${routine.cooldownSeconds}s"
                    else if (routine.cooldownMinutes > 0) "${routine.cooldownMinutes}m"
                    else "${routine.cooldownSeconds}s"
                    "+ $cStr " + stringResource(R.string.timer_phase_cooldown)
                } else null

                val extraText = listOfNotNull(warmupPart, cooldownPart).joinToString("   •   ")

                Text(
                    text = extraText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Edit & Delete (Subtle left), Start Button (Tactile Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onViewLog,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.History,
                            contentDescription = stringResource(R.string.routine_view_log),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.routine_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.btn_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                TactilePillButton(
                    onClick = onPlay,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 6.dp,
                    useBrandGradient = true
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.start_timer),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
