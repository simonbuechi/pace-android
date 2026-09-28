package ch.simibu.pace.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.service.PaceTimerService
import ch.simibu.pace.ui.components.CircularTimerRing
import ch.simibu.pace.ui.theme.PhaseBreakColor
import ch.simibu.pace.ui.theme.PhaseCompletedColor
import ch.simibu.pace.ui.theme.PhaseFocusColor
import ch.simibu.pace.ui.theme.PhaseWarmupColor

@Composable
fun TimerScreen(
    onExitTimer: () -> Unit
) {
    val context = LocalContext.current
    val timerEngine = PaceApplication.instance.timerEngine
    val settingsRepo = PaceApplication.instance.settingsRepository

    val timerState by timerEngine.state.collectAsState()
    val screenAwake by settingsRepo.screenAwake.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }

    // Screen Keep Awake effect
    DisposableEffect(screenAwake) {
        val activity = context as? Activity
        if (screenAwake) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Intercept back button to confirm exit
    BackHandler {
        if (timerState.isRunning && !timerState.isCompleted) {
            showExitDialog = true
        } else {
            onExitTimer()
        }
    }

    val phaseColor = when (timerState.phase) {
        TimerPhase.WARMUP -> PhaseWarmupColor
        TimerPhase.FOCUS -> timerState.colorScheme.primaryColor
        TimerPhase.BREAK -> PhaseBreakColor
        TimerPhase.COMPLETED -> PhaseCompletedColor
    }

    val animatedPhaseColor by animateColorAsState(
        targetValue = phaseColor,
        animationSpec = tween(600),
        label = "bg_phase_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedPhaseColor.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        if (timerState.isRunning && !timerState.isCompleted) {
                            showExitDialog = true
                        } else {
                            onExitTimer()
                        }
                    },
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Exit timer")
                }

                Text(
                    text = timerState.routineName.ifBlank { stringResource(R.string.app_short_name) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Placeholder to balance the top bar layout
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Phase and Round Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phase badge
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = animatedPhaseColor.copy(alpha = 0.18f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(timerState.phase.titleRes).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = animatedPhaseColor,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                if (!timerState.isCompleted) {
                    Text(
                        text = stringResource(
                            R.string.round_indicator,
                            timerState.currentRound,
                            timerState.totalRounds
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Center Circular Timer
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                CircularTimerRing(
                    modifier = Modifier.fillMaxSize(),
                    progress = timerState.progress,
                    phase = timerState.phase,
                    remainingSeconds = timerState.remainingSecondsInPhase,
                    accentColor = timerState.colorScheme.primaryColor
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = timerState.formattedRemainingTime,
                            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                            label = "timer_digits"
                        ) { digits ->
                            Text(
                                text = if (timerState.isCompleted) "✓" else digits,
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (timerState.isCompleted) PhaseCompletedColor else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (!timerState.isCompleted) {
                            Text(
                                text = stringResource(R.string.time_remaining),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Bottom Controls Area
            if (timerState.isCompleted) {
                // Completed Screen Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.session_finished_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = PhaseCompletedColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.session_finished_msg, timerState.totalRounds),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                timerEngine.restart()
                                PaceTimerService.start(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.Replay, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.btn_restart))
                        }

                        Button(
                            onClick = {
                                PaceTimerService.stop(context)
                                onExitTimer()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PhaseCompletedColor)
                        ) {
                            Text(stringResource(R.string.dialog_confirm))
                        }
                    }
                }
            } else {
                // Active Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Restart Button
                    FilledTonalIconButton(
                        onClick = { timerEngine.restart() },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Rounded.Replay, contentDescription = stringResource(R.string.btn_restart))
                    }

                    // Main Play/Pause Button
                    FilledIconButton(
                        onClick = { timerEngine.togglePauseResume() },
                        modifier = Modifier.size(84.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = animatedPhaseColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (timerState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (timerState.isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Skip Phase Button
                    FilledTonalIconButton(
                        onClick = { timerEngine.skipPhase() },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Rounded.FastForward, contentDescription = stringResource(R.string.btn_skip))
                    }
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_timer_title)) },
            text = { Text(stringResource(R.string.exit_timer_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        timerEngine.stop()
                        PaceTimerService.stop(context)
                        onExitTimer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}
