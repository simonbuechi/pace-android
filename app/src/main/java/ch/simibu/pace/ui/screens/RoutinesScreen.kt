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
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry
import ch.simibu.pace.ui.theme.PhaseCooldownColor
import ch.simibu.pace.ui.theme.PhaseWarmupColor

@Composable
fun RoutinesScreen(
    onStartRoutine: (Routine) -> Unit,
    showNewRoutineDialog: Boolean = false,
    onResetShowNewDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val routineRepo = PaceApplication.instance.routineRepository
    val timerEngine = PaceApplication.instance.timerEngine

    val routines by routineRepo.routines.collectAsState()

    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
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
                            onEdit = { editingRoutine = routine },
                            onDelete = { routineToDelete = routine }
                        )
                    }
                }
            }
        }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Color bar, Name, Category chip, Action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (routine.colorScheme == ch.simibu.pace.model.ColorSchemeOption.PACE) PaceBrandGradient
                                else androidx.compose.ui.graphics.SolidColor(routine.colorScheme.primaryColor)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Warm-up badge if set
                    if (routine.totalWarmupSeconds > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PhaseWarmupColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = stringResource(R.string.badge_warmup, routine.warmupMinutes, routine.warmupSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PhaseWarmupColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Focus badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = routine.colorScheme.primaryColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = stringResource(R.string.badge_focus, routine.focusMinutes, routine.focusSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = routine.colorScheme.primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    // Break badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = stringResource(R.string.badge_break, routine.breakMinutes, routine.breakSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    // Cool-down badge if set
                    if (routine.totalCooldownSeconds > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PhaseCooldownColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = stringResource(R.string.badge_cooldown, routine.cooldownMinutes, routine.cooldownSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PhaseCooldownColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Rounds badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${routine.iterations}x",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                // Play Button
                TactilePillButton(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onPlay() },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (routine.colorScheme == ch.simibu.pace.model.ColorSchemeOption.PACE) PaceBrandGradient
                                else androidx.compose.ui.graphics.SolidColor(routine.colorScheme.primaryColor)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Start routine",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
