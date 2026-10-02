package ch.simibu.pace.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.RoutineLogEntry
import ch.simibu.pace.service.PaceTimerService
import ch.simibu.pace.ui.components.TactileCard
import ch.simibu.pace.ui.components.TactileCircleButton
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.components.TactileSunkenWell
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceRaspberry

@OptIn(ExperimentalSharedTransitionApi::class)
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

    var selectedRoutineId by remember { mutableStateOf<String?>(null) }
    val selectedRoutine = routines.find { it.id == selectedRoutineId }

    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
    var viewingLogRoutine by remember { mutableStateOf<Routine?>(null) }
    var isAddingNew by remember { mutableStateOf(showNewRoutineDialog) }
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }

    BackHandler(enabled = selectedRoutine != null) {
        selectedRoutineId = null
    }

    Scaffold(
        floatingActionButton = {
            if (selectedRoutine == null) {
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
        }
    ) { padding ->
        SharedTransitionLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = selectedRoutine,
                transitionSpec = {
                    if (targetState != null) {
                        // Expanding container transform into detail view
                        (fadeIn(animationSpec = tween(220, delayMillis = 60)) +
                                scaleIn(
                                    initialScale = 0.93f,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMediumLow,
                                        dampingRatio = Spring.DampingRatioLowBouncy
                                    )
                                ))
                            .togetherWith(
                                fadeOut(animationSpec = tween(90)) +
                                        scaleOut(targetScale = 1.04f, animationSpec = tween(140))
                            )
                    } else {
                        // Collapsing container transform back into routines list
                        (fadeIn(animationSpec = tween(200, delayMillis = 60)) +
                                scaleIn(initialScale = 1.04f, animationSpec = tween(200)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(90)) +
                                        scaleOut(targetScale = 0.93f, animationSpec = tween(160))
                            )
                    }
                },
                label = "RoutineContainerTransform"
            ) { targetRoutine ->
                if (targetRoutine == null) {
                    // Denser Routines List View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.routines_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(14.dp))

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
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(routines, key = { it.id }) { routine ->
                                    RoutineCard(
                                        modifier = Modifier.sharedBounds(
                                            rememberSharedContentState(key = "card-${routine.id}"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            boundsTransform = { _, _ ->
                                                spring(
                                                    dampingRatio = 0.82f,
                                                    stiffness = 380f
                                                )
                                            }
                                        ),
                                        routine = routine,
                                        onClick = { selectedRoutineId = routine.id },
                                        onPlay = {
                                            timerEngine.startRoutine(routine)
                                            PaceTimerService.start(context)
                                            onStartRoutine(routine)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Routine Detailed View with full container transform
                    RoutineDetailView(
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(key = "card-${targetRoutine.id}"),
                            animatedVisibilityScope = this@AnimatedContent,
                            boundsTransform = { _, _ ->
                                spring(
                                    dampingRatio = 0.82f,
                                    stiffness = 380f
                                )
                            }
                        ),
                        routine = targetRoutine,
                        logs = logs,
                        onBack = { selectedRoutineId = null },
                        onPlay = {
                            timerEngine.startRoutine(targetRoutine)
                            PaceTimerService.start(context)
                            onStartRoutine(targetRoutine)
                        },
                        onViewLog = { viewingLogRoutine = targetRoutine },
                        onEdit = { editingRoutine = targetRoutine },
                        onDelete = { routineToDelete = targetRoutine }
                    )
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
                        if (selectedRoutineId == routine.id) {
                            selectedRoutineId = null
                        }
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

/**
 * Dense Routine Card for the list view.
 * Features compact spacing, title + color accent, key metrics, and a fast start icon button.
 */
@Composable
fun RoutineCard(
    routine: Routine,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    TactileCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Routine Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Header row: Color accent dot + Routine Name + Category pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(routine.colorScheme.primaryColor)
                    )
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (routine.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = routine.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Metrics Row: Focus • Break • Iterations (Total Duration)
                val focusStr = formatMinutesSeconds(routine.focusMinutes, routine.focusSeconds)
                val breakStr = formatMinutesSeconds(routine.breakMinutes, routine.breakSeconds)
                val durationText = formatTotalDuration(routine.totalDurationSeconds)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = focusStr,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = breakStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "${routine.iterations}x",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = PaceRaspberry
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Tactile Start Play Icon Button
            TactilePillButton(
                onClick = onPlay,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                elevation = 5.dp,
                useBrandGradient = true
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.start_timer),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Routine Detail View showing all details of the selected routine.
 * Accessible via container transform from the list card.
 */
@Composable
fun RoutineDetailView(
    routine: Routine,
    logs: List<RoutineLogEntry>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onViewLog: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Navigation & Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TactileCircleButton(
                onClick = onBack,
                size = 40.dp,
                elevation = 2.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.btn_close),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = stringResource(R.string.routine_details_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(onClick = onViewLog, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.History,
                        contentDescription = stringResource(R.string.routine_view_log),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.routine_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.btn_delete),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Card: Routine Name, Category, Total Duration
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(routine.colorScheme.primaryColor)
                            )
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (routine.category.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = routine.colorScheme.primaryColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = routine.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = routine.colorScheme.primaryColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TactileSunkenWell(shape = RoundedCornerShape(14.dp)) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.routine_total_duration),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = formatTotalDuration(routine.totalDurationSeconds),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PaceRaspberry
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual Interval Distribution Bar
                val focusSeconds = routine.totalFocusSeconds
                val breakSeconds = routine.totalBreakSeconds
                val sumSec = (focusSeconds + breakSeconds).coerceAtLeast(1)
                val focusFraction = focusSeconds.toFloat() / sumSec

                TactileSunkenWell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(focusFraction.coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .background(routine.colorScheme.primaryColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - focusFraction).coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.45f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 1: Phases & Intervals Breakdown
        DetailSectionCard(title = stringResource(R.string.quick_start_title)) {
            if (routine.totalWarmupSeconds > 0) {
                DetailItemRow(
                    label = stringResource(R.string.timer_phase_warmup),
                    value = formatMinutesSeconds(routine.warmupMinutes, routine.warmupSeconds),
                    dotColor = Color(0xFFF59E0B)
                )
            }
            DetailItemRow(
                label = stringResource(R.string.timer_phase_focus),
                value = formatMinutesSeconds(routine.focusMinutes, routine.focusSeconds),
                dotColor = routine.colorScheme.primaryColor
            )
            DetailItemRow(
                label = stringResource(R.string.timer_phase_break),
                value = formatMinutesSeconds(routine.breakMinutes, routine.breakSeconds),
                dotColor = Color.Gray
            )
            DetailItemRow(
                label = stringResource(R.string.iterations),
                value = stringResource(R.string.rounds_count, routine.iterations),
                dotColor = PaceRaspberry
            )
            if (routine.totalCooldownSeconds > 0) {
                DetailItemRow(
                    label = stringResource(R.string.timer_phase_cooldown),
                    value = formatMinutesSeconds(routine.cooldownMinutes, routine.cooldownSeconds),
                    dotColor = Color(0xFF3B82F6)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Audio & Countdown Signals
        DetailSectionCard(title = stringResource(R.string.sound_scheme_label)) {
            DetailItemRow(
                label = stringResource(R.string.sound_scheme_label),
                value = stringResource(routine.soundScheme.titleRes)
            )
            DetailItemRow(
                label = stringResource(R.string.routine_countdown_signal_label),
                value = if (routine.countdownSignalEnabled) {
                    stringResource(R.string.routine_countdown_chip, routine.countdownSignalSeconds)
                } else {
                    stringResource(R.string.sound_none)
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Visual Theme & Motion
        DetailSectionCard(title = stringResource(R.string.color_scheme_label)) {
            DetailItemRow(
                label = stringResource(R.string.color_scheme_label),
                value = stringResource(routine.colorScheme.titleRes),
                dotColor = routine.colorScheme.primaryColor
            )
            DetailItemRow(
                label = stringResource(R.string.routine_bg_animation_label),
                value = stringResource(routine.backgroundAnimation.titleRes)
            )
        }

        // Section 4: Completion History
        val routineLogs = logs.filter { it.routineId == routine.id }
        if (routineLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            DetailSectionCard(title = stringResource(R.string.routine_log_title)) {
                DetailItemRow(
                    label = stringResource(R.string.routine_log_title),
                    value = stringResource(R.string.routine_log_completed_count, routineLogs.size)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Start Session CTA
        TactilePillButton(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = 6.dp,
            useBrandGradient = true
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.start_timer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            content()
        }
    }
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    dotColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatMinutesSeconds(minutes: Int, seconds: Int): String {
    return if (minutes > 0 && seconds > 0) "${minutes}m ${seconds}s"
    else if (minutes > 0) "${minutes}m"
    else "${seconds}s"
}

private fun formatTotalDuration(totalSec: Int): String {
    val mins = totalSec / 60
    val secs = totalSec % 60
    return if (mins > 0 && secs > 0) "${mins}m ${secs}s"
    else if (mins > 0) "${mins} min"
    else "${secs} sec"
}
