package ch.simibu.pace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import ch.simibu.pace.ui.components.TactileCard
import ch.simibu.pace.ui.components.TactileCircleButton
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.components.TactileSunkenWell
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.SoundScheme
import ch.simibu.pace.service.PaceTimerService
import ch.simibu.pace.ui.components.TimeDurationWheelPicker

@Composable
fun QuickStartScreen(
    onStartSession: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepo = PaceApplication.instance.settingsRepository
    val timerEngine = PaceApplication.instance.timerEngine

    var focusMinutes by remember { mutableIntStateOf(settingsRepo.quickFocusMinutes) }
    var focusSeconds by remember { mutableIntStateOf(settingsRepo.quickFocusSeconds) }
    var breakMinutes by remember { mutableIntStateOf(settingsRepo.quickBreakMinutes) }
    var breakSeconds by remember { mutableIntStateOf(settingsRepo.quickBreakSeconds) }
    var iterations by remember { mutableIntStateOf(settingsRepo.quickIterations) }

    val scrollState = rememberScrollState()

    fun updateAndPersist() {
        settingsRepo.quickFocusMinutes = focusMinutes
        settingsRepo.quickFocusSeconds = focusSeconds
        settingsRepo.quickBreakMinutes = breakMinutes
        settingsRepo.quickBreakSeconds = breakSeconds
        settingsRepo.quickIterations = iterations
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.quick_start_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.quick_start_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time Pickers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeDurationWheelPicker(
                modifier = Modifier.weight(1f),
                minutes = focusMinutes,
                seconds = focusSeconds,
                onMinutesChanged = {
                    focusMinutes = it
                    updateAndPersist()
                },
                onSecondsChanged = {
                    focusSeconds = it
                    updateAndPersist()
                },
                title = stringResource(R.string.focus_duration)
            )

            TimeDurationWheelPicker(
                modifier = Modifier.weight(1f),
                minutes = breakMinutes,
                seconds = breakSeconds,
                onMinutesChanged = {
                    breakMinutes = it
                    updateAndPersist()
                },
                onSecondsChanged = {
                    breakSeconds = it
                    updateAndPersist()
                },
                title = stringResource(R.string.break_duration)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Iterations / Rounds Tactile Card
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.iterations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TactileCircleButton(
                        onClick = {
                            if (iterations > 1) {
                                iterations--
                                updateAndPersist()
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        elevation = 6.dp
                    ) {
                        Icon(
                            Icons.Rounded.Remove,
                            contentDescription = "Decrease rounds",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TactileSunkenWell(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.rounds_count, iterations),
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.Black,
                            color = PaceRaspberry,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                        )
                    }

                    TactileCircleButton(
                        onClick = {
                            if (iterations < 99) {
                                iterations++
                                updateAndPersist()
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        elevation = 6.dp
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "Increase rounds",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Big Primary Action: Start Session with Tactile Pill Button & Brand Gradient
        TactilePillButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            onClick = {
                val totalFocus = (focusMinutes * 60) + focusSeconds
                val totalBreak = (breakMinutes * 60) + breakSeconds

                timerEngine.startQuickTimer(
                    focusSec = if (totalFocus > 0) totalFocus else 45,
                    breakSec = if (totalBreak > 0) totalBreak else 15,
                    rounds = iterations,
                    warmupSec = 0,
                    focusSound = settingsRepo.focusSoundScheme.value,
                    focusRepeats = settingsRepo.focusSoundRepeats.value,
                    breakSound = settingsRepo.breakSoundScheme.value,
                    breakRepeats = settingsRepo.breakSoundRepeats.value,
                    color = ColorSchemeOption.PACE
                )
                PaceTimerService.start(context)
                onStartSession()
            },
            shape = RoundedCornerShape(22.dp),
            elevation = 10.dp,
            useBrandGradient = true
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.start_timer),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
