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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.service.PaceTimerService
import ch.simibu.pace.ui.components.CircularTimerRing
import ch.simibu.pace.ui.components.TactileCard
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.components.TactileSunkenWell
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceDarkShadowDark
import ch.simibu.pace.ui.theme.PaceDarkSurfaceSunken
import ch.simibu.pace.ui.theme.PaceLightShadowDark
import ch.simibu.pace.ui.theme.PaceLightSurfaceSunken
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry
import ch.simibu.pace.ui.theme.PhaseBreakColor
import ch.simibu.pace.ui.theme.PhaseCompletedColor
import ch.simibu.pace.ui.theme.PhaseCooldownColor
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
        TimerPhase.FOCUS -> PaceRaspberry
        TimerPhase.BREAK -> PhaseBreakColor
        TimerPhase.COOLDOWN -> PhaseCooldownColor
        TimerPhase.COMPLETED -> PaceMagenta
    }

    val animatedPhaseColor by animateColorAsState(
        targetValue = phaseColor,
        animationSpec = tween(600),
        label = "bg_phase_color"
    )

    val isDark = ch.simibu.pace.ui.components.isTactileThemeDark()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedPhaseColor.copy(alpha = if (isDark) 0.16f else 0.10f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .systemBarsPadding()
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // Widescreen / Landscape Layout: Maximal font size readable across the room
            LandscapeTimerLayout(
                timerState = timerState,
                phaseColor = animatedPhaseColor,
                isDark = isDark,
                onExitRequest = {
                    if (timerState.isRunning && !timerState.isCompleted) {
                        showExitDialog = true
                    } else {
                        onExitTimer()
                    }
                },
                onTogglePlay = { timerEngine.togglePauseResume() },
                onSkip = { timerEngine.skipPhase() },
                onRestart = {
                    timerEngine.restart()
                    PaceTimerService.start(context)
                },
                onDone = {
                    PaceTimerService.stop(context)
                    onExitTimer()
                }
            )
        } else {
            // Portrait Layout: Centered ring with maximal digit size
            PortraitTimerLayout(
                timerState = timerState,
                phaseColor = animatedPhaseColor,
                isDark = isDark,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                onExitRequest = {
                    if (timerState.isRunning && !timerState.isCompleted) {
                        showExitDialog = true
                    } else {
                        onExitTimer()
                    }
                },
                onTogglePlay = { timerEngine.togglePauseResume() },
                onSkip = { timerEngine.skipPhase() },
                onRestart = {
                    timerEngine.restart()
                    PaceTimerService.start(context)
                },
                onDone = {
                    PaceTimerService.stop(context)
                    onExitTimer()
                }
            )
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

@Composable
private fun PortraitTimerLayout(
    timerState: ch.simibu.pace.model.TimerState,
    phaseColor: Color,
    isDark: Boolean,
    maxWidth: androidx.compose.ui.unit.Dp,
    maxHeight: androidx.compose.ui.unit.Dp,
    onExitRequest: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkip: () -> Unit,
    onRestart: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileCard(
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onExitRequest() },
                shape = CircleShape,
                elevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Exit timer",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Text(
                text = timerState.routineName.ifBlank { stringResource(R.string.app_short_name) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.size(46.dp))
        }

        // Phase & Round Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TactileSunkenWell(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(
                    text = stringResource(timerState.phase.titleRes).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = phaseColor,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 9.dp)
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Center Ring with Maximal Display Typography
        val ringDiameter = min(maxWidth - 32.dp, maxHeight * 0.44f).coerceAtMost(350.dp)
        val digitFontSize = (ringDiameter.value * 0.27f).sp

        Box(
            modifier = Modifier
                .size(ringDiameter)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            CircularTimerRing(
                modifier = Modifier.fillMaxSize(),
                progress = timerState.progress,
                phase = timerState.phase,
                remainingSeconds = timerState.remainingSecondsInPhase,
                accentColor = PaceRaspberry,
                strokeWidth = 18.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = timerState.formattedRemainingTime,
                        transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(120)) },
                        label = "timer_digits_portrait"
                    ) { digits ->
                        Text(
                            text = if (timerState.isCompleted) "✓" else digits,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = digitFontSize,
                                fontFamily = FontFamily.Default
                            ),
                            fontWeight = FontWeight.Black,
                            color = if (timerState.isCompleted) PhaseCompletedColor else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (!timerState.isCompleted) {
                        Text(
                            text = stringResource(R.string.time_remaining),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom Controls
        if (timerState.isCompleted) {
            CompletedActions(
                totalRounds = timerState.totalRounds,
                onRestart = onRestart,
                onDone = onDone
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tactile Restart Button
                TactileCard(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onRestart() },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Replay,
                            contentDescription = stringResource(R.string.btn_restart),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Tactile Play/Pause Pill Button with Brand Gradient
                TactilePillButton(
                    modifier = Modifier
                        .size(86.dp)
                        .clickable { onTogglePlay() },
                    shape = CircleShape,
                    elevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PaceBrandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (timerState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (timerState.isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause),
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                // Tactile Skip Button
                TactileCard(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onSkip() },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.FastForward,
                            contentDescription = stringResource(R.string.btn_skip),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapeTimerLayout(
    timerState: ch.simibu.pace.model.TimerState,
    phaseColor: Color,
    isDark: Boolean,
    onExitRequest: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkip: () -> Unit,
    onRestart: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left & Center Panoramic Display (Giant Digits readable across gym room)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Header: Phase badge & Round Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                TactileSunkenWell(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(timerState.phase.titleRes).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = phaseColor,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                    )
                }

                if (!timerState.isCompleted) {
                    Text(
                        text = stringResource(
                            R.string.round_indicator,
                            timerState.currentRound,
                            timerState.totalRounds
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = timerState.routineName.ifBlank { stringResource(R.string.app_short_name) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Giant Countdown Digits (Maximal font size for widescreen view)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (timerState.isCompleted) {
                    Column {
                        Text(
                            text = stringResource(R.string.session_finished_title),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PhaseCompletedColor
                        )
                        Text(
                            text = stringResource(R.string.session_finished_msg, timerState.totalRounds),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = timerState.formattedRemainingTime,
                        transitionSpec = { fadeIn(tween(100)) togetherWith fadeOut(tween(100)) },
                        label = "timer_digits_landscape"
                    ) { digits ->
                        Text(
                            text = digits,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 135.sp,
                                lineHeight = 135.sp,
                                letterSpacing = (-2).sp
                            ),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Tactile Smooth Horizontal Progress Bar
            val sunkenTrackBg = if (isDark) PaceDarkSurfaceSunken else PaceLightSurfaceSunken
            val sunkenBorder = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(sunkenTrackBg)
                    .border(1.dp, sunkenBorder, RoundedCornerShape(7.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(timerState.progress.coerceIn(0f, 1f))
                        .background(PaceBrandGradient)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Right Vertical Controls Panel
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Exit Button
            TactileCard(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onExitRequest() },
                shape = CircleShape,
                elevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Exit timer",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (timerState.isCompleted) {
                TactilePillButton(
                    modifier = Modifier
                        .height(56.dp)
                        .clickable { onRestart() },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(PaceBrandGradient)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Replay, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_restart), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Restart button
                TactileCard(
                    modifier = Modifier
                        .size(54.dp)
                        .clickable { onRestart() },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Replay,
                            contentDescription = stringResource(R.string.btn_restart),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Play/Pause Pill Button
                TactilePillButton(
                    modifier = Modifier
                        .size(76.dp)
                        .clickable { onTogglePlay() },
                    shape = CircleShape,
                    elevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PaceBrandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (timerState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (timerState.isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause),
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Skip button
                TactileCard(
                    modifier = Modifier
                        .size(54.dp)
                        .clickable { onSkip() },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.FastForward,
                            contentDescription = stringResource(R.string.btn_skip),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedActions(
    totalRounds: Int,
    onRestart: () -> Unit,
    onDone: () -> Unit
) {
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
            text = stringResource(R.string.session_finished_msg, totalRounds),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TactileCard(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable { onRestart() },
                shape = RoundedCornerShape(18.dp),
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Replay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.btn_restart),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            TactilePillButton(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable { onDone() },
                shape = RoundedCornerShape(18.dp),
                elevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PaceBrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.dialog_confirm),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
