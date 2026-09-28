package ch.simibu.pace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.simibu.pace.R
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.SoundScheme
import ch.simibu.pace.ui.components.TimeDurationWheelPicker
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorDialog(
    initialRoutine: Routine? = null,
    defaultFocusMin: Int = 0,
    defaultFocusSec: Int = 45,
    defaultBreakMin: Int = 0,
    defaultBreakSec: Int = 15,
    defaultRounds: Int = 8,
    defaultWarmupSec: Int = 0,
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    var name by remember { mutableStateOf(initialRoutine?.name ?: "") }
    var category by remember { mutableStateOf(initialRoutine?.category ?: "Workout") }
    var focusMin by remember { mutableIntStateOf(initialRoutine?.focusMinutes ?: defaultFocusMin) }
    var focusSec by remember { mutableIntStateOf(initialRoutine?.focusSeconds ?: defaultFocusSec) }
    var breakMin by remember { mutableIntStateOf(initialRoutine?.breakMinutes ?: defaultBreakMin) }
    var breakSec by remember { mutableIntStateOf(initialRoutine?.breakSeconds ?: defaultBreakSec) }
    var iterations by remember { mutableIntStateOf(initialRoutine?.iterations ?: defaultRounds) }
    var warmupEnabled by remember { mutableStateOf((initialRoutine?.warmupSeconds ?: defaultWarmupSec) > 0) }
    var selectedSound by remember { mutableStateOf(initialRoutine?.soundScheme ?: SoundScheme.BEEP) }
    var selectedColor by remember { mutableStateOf(initialRoutine?.colorScheme ?: ColorSchemeOption.PACE) }

    var soundDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRoutine != null) stringResource(R.string.routine_edit) else stringResource(R.string.routine_new),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.routine_name_label)) },
                    placeholder = { Text(stringResource(R.string.routine_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Workout", "HIIT", "Pomodoro", "Recovery").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeDurationWheelPicker(
                        modifier = Modifier.weight(1f),
                        minutes = focusMin,
                        seconds = focusSec,
                        onMinutesChanged = { focusMin = it },
                        onSecondsChanged = { focusSec = it },
                        title = stringResource(R.string.focus_duration)
                    )

                    TimeDurationWheelPicker(
                        modifier = Modifier.weight(1f),
                        minutes = breakMin,
                        seconds = breakSec,
                        onMinutesChanged = { breakMin = it },
                        onSecondsChanged = { breakSec = it },
                        title = stringResource(R.string.break_duration)
                    )
                }

                // Rounds / Iterations Stepper
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.iterations),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (iterations > 1) iterations-- },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = null)
                        }

                        Text(
                            text = stringResource(R.string.rounds_count, iterations),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        FilledTonalIconButton(
                            onClick = { if (iterations < 99) iterations++ },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                        }
                    }
                }

                // Warmup Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.warmup_toggle))
                    Switch(
                        checked = warmupEnabled,
                        onCheckedChange = { warmupEnabled = it }
                    )
                }

                // Sound Scheme Dropdown
                ExposedDropdownMenuBox(
                    expanded = soundDropdownExpanded,
                    onExpandedChange = { soundDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = stringResource(selectedSound.titleRes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.sound_scheme_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soundDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = soundDropdownExpanded,
                        onDismissRequest = { soundDropdownExpanded = false }
                    ) {
                        SoundScheme.entries.forEach { scheme ->
                            DropdownMenuItem(
                                text = { Text(stringResource(scheme.titleRes)) },
                                onClick = {
                                    selectedSound = scheme
                                    soundDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Color Scheme Selection
                Column {
                    Text(
                        text = stringResource(R.string.color_scheme_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ColorSchemeOption.entries.forEach { colorOption ->
                            val isSelected = selectedColor == colorOption
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colorOption.primaryColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorOption },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (name.isBlank()) "Routine" else name.trim()
                    val routine = Routine(
                        id = initialRoutine?.id ?: UUID.randomUUID().toString(),
                        name = finalName,
                        category = category,
                        focusMinutes = focusMin,
                        focusSeconds = focusSec,
                        breakMinutes = breakMin,
                        breakSeconds = breakSec,
                        iterations = iterations,
                        warmupSeconds = if (warmupEnabled) 10 else 0,
                        soundSchemeId = selectedSound.id,
                        colorSchemeId = selectedColor.id
                    )
                    onSave(routine)
                }
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}
