package ch.simibu.pace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.data.SettingsRepository
import ch.simibu.pace.model.BackgroundAnimationOption
import ch.simibu.pace.model.SoundScheme
import ch.simibu.pace.ui.components.TactileCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val settingsRepo = PaceApplication.instance.settingsRepository
    val soundManager = PaceApplication.instance.soundManager
    val themeMode by settingsRepo.themeMode.collectAsState()
    val soundEnabled by settingsRepo.soundEnabled.collectAsState()
    val focusSoundScheme by settingsRepo.focusSoundScheme.collectAsState()
    val focusSoundRepeats by settingsRepo.focusSoundRepeats.collectAsState()
    val breakSoundScheme by settingsRepo.breakSoundScheme.collectAsState()
    val breakSoundRepeats by settingsRepo.breakSoundRepeats.collectAsState()
    val vibrationEnabled by settingsRepo.vibrationEnabled.collectAsState()
    val screenAwake by settingsRepo.screenAwake.collectAsState()
    val backgroundAnimation by settingsRepo.backgroundAnimation.collectAsState()

    var focusDropdownExpanded by remember { mutableStateOf(false) }
    var breakDropdownExpanded by remember { mutableStateOf(false) }
    var bgAnimationDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Appearance / Theme Card
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (themeMode == SettingsRepository.THEME_DARK) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(R.string.setting_theme_mode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val options = listOf(
                        SettingsRepository.THEME_SYSTEM to R.string.theme_system,
                        SettingsRepository.THEME_LIGHT to R.string.theme_light,
                        SettingsRepository.THEME_DARK to R.string.theme_dark
                    )
                    options.forEachIndexed { index, (mode, labelRes) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            onClick = { settingsRepo.setThemeMode(mode) },
                            selected = themeMode == mode
                        ) {
                            Text(stringResource(labelRes))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Timer Background Animation Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.setting_bg_animation_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.setting_bg_animation_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = bgAnimationDropdownExpanded,
                    onExpandedChange = { bgAnimationDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = stringResource(backgroundAnimation.titleRes),
                        onValueChange = {},
                        readOnly = true,
                        supportingText = { Text(stringResource(backgroundAnimation.descRes)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgAnimationDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bgAnimationDropdownExpanded,
                        onDismissRequest = { bgAnimationDropdownExpanded = false }
                    ) {
                        BackgroundAnimationOption.GLOBAL_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = stringResource(option.titleRes),
                                            fontWeight = if (backgroundAnimation == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = stringResource(option.descRes),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    settingsRepo.setBackgroundAnimation(option)
                                    bgAnimationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound & Haptics Card
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sound switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.sound_scheme_label),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.setting_sound_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { settingsRepo.setSoundEnabled(it) }
                    )
                }

                // Sound choices: Focus and Break with preview & 1-5x repeats
                AnimatedVisibility(
                    visible = soundEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Focus Sound
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.setting_focus_sound),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(R.string.setting_focus_sound_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = focusDropdownExpanded,
                                    onExpandedChange = { focusDropdownExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = stringResource(focusSoundScheme.titleRes),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.setting_focus_sound)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = focusDropdownExpanded) },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = focusDropdownExpanded,
                                        onDismissRequest = { focusDropdownExpanded = false }
                                    ) {
                                        SoundScheme.entries.forEach { scheme ->
                                            DropdownMenuItem(
                                                text = { Text(stringResource(scheme.titleRes)) },
                                                onClick = {
                                                    settingsRepo.setFocusSoundScheme(scheme)
                                                    focusDropdownExpanded = false
                                                    if (scheme != SoundScheme.NONE) {
                                                        soundManager.playSchemeSound(scheme, repeats = focusSoundRepeats)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                FilledTonalIconButton(
                                    onClick = {
                                        soundManager.playSchemeSound(focusSoundScheme, repeats = focusSoundRepeats)
                                    },
                                    enabled = focusSoundScheme != SoundScheme.NONE,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = stringResource(R.string.setting_focus_sound)
                                    )
                                }
                            }

                            // Focus Repeat Count (1-5)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.setting_sound_repeats),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (focusSoundRepeats == 1) {
                                            stringResource(R.string.repeats_count_1)
                                        } else {
                                            stringResource(R.string.repeats_count_n, focusSoundRepeats)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    (1..5).forEachIndexed { index, count ->
                                        SegmentedButton(
                                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 5),
                                            onClick = {
                                                settingsRepo.setFocusSoundRepeats(count)
                                                if (focusSoundScheme != SoundScheme.NONE) {
                                                    soundManager.playSchemeSound(focusSoundScheme, repeats = count)
                                                }
                                            },
                                            selected = focusSoundRepeats == count
                                        ) {
                                            Text("${count}x")
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Break Sound
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.setting_break_sound),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(R.string.setting_break_sound_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = breakDropdownExpanded,
                                    onExpandedChange = { breakDropdownExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = stringResource(breakSoundScheme.titleRes),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.setting_break_sound)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breakDropdownExpanded) },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = breakDropdownExpanded,
                                        onDismissRequest = { breakDropdownExpanded = false }
                                    ) {
                                        SoundScheme.entries.forEach { scheme ->
                                            DropdownMenuItem(
                                                text = { Text(stringResource(scheme.titleRes)) },
                                                onClick = {
                                                    settingsRepo.setBreakSoundScheme(scheme)
                                                    breakDropdownExpanded = false
                                                    if (scheme != SoundScheme.NONE) {
                                                        soundManager.playSchemeSound(scheme, repeats = breakSoundRepeats)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                FilledTonalIconButton(
                                    onClick = {
                                        soundManager.playSchemeSound(breakSoundScheme, repeats = breakSoundRepeats)
                                    },
                                    enabled = breakSoundScheme != SoundScheme.NONE,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = stringResource(R.string.setting_break_sound)
                                    )
                                }
                            }

                            // Break Repeat Count (1-5)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.setting_sound_repeats),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (breakSoundRepeats == 1) {
                                            stringResource(R.string.repeats_count_1)
                                        } else {
                                            stringResource(R.string.repeats_count_n, breakSoundRepeats)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    (1..5).forEachIndexed { index, count ->
                                        SegmentedButton(
                                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 5),
                                            onClick = {
                                                settingsRepo.setBreakSoundRepeats(count)
                                                if (breakSoundScheme != SoundScheme.NONE) {
                                                    soundManager.playSchemeSound(breakSoundScheme, repeats = count)
                                                }
                                            },
                                            selected = breakSoundRepeats == count
                                        ) {
                                            Text("${count}x")
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Vibration switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.setting_vibration),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.setting_vibration_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { settingsRepo.setVibrationEnabled(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screen Awake Card
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Rounded.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.setting_screen_awake),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.setting_screen_awake_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = screenAwake,
                    onCheckedChange = { settingsRepo.setScreenAwake(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Information Card
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.setting_language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.setting_language_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // App Version
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pace Amigo v1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
