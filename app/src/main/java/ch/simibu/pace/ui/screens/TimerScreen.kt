package ch.simibu.pace.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.ui.draw.scale
import ch.simibu.pace.ui.components.ConfettiEffect
import ch.simibu.pace.ui.components.PhaseRippleEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.model.BackgroundAnimationOption
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.service.PaceTimerService
import ch.simibu.pace.ui.components.AnimatedTimerBackground
import ch.simibu.pace.ui.components.CircularTimerRing
import ch.simibu.pace.ui.components.TactileCard
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.components.TactileSunkenWell
import ch.simibu.pace.ui.components.isTactileThemeDark
import ch.simibu.pace.ui.theme.PaceBrandGradient
import ch.simibu.pace.ui.theme.PaceMagenta
import ch.simibu.pace.ui.theme.PaceRaspberry
import ch.simibu.pace.ui.theme.PhaseBreakColor
import ch.simibu.pace.ui.theme.PhaseCompletedColor
import ch.simibu.pace.ui.theme.PhaseCooldownColor
import ch.simibu.pace.ui.theme.PhaseWarmupColor
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    onExitTimer: () -> Unit
) {
    val context = LocalContext.current
    val timerEngine = PaceApplication.instance.timerEngine
    val settingsRepo = PaceApplication.instance.settingsRepository

    val timerState by timerEngine.state.collectAsState()
    val screenAwake by settingsRepo.screenAwake.collectAsState()
    val globalAnimation by settingsRepo.backgroundAnimation.collectAsState()

    val effectiveAnimation = if (timerState.backgroundAnimation != BackgroundAnimationOption.APP_DEFAULT) {
        timerState.backgroundAnimation
    } else {
        globalAnimation
    }

    var showExitDialog by remember { mutableStateOf(false) }

    // When timer is running, buttons are hidden by default. Once clicked, buttons appear.
    // If paused or completed, buttons stay visible.
    var controlsVisible by remember { mutableStateOf(timerState.isPaused || !timerState.isRunning || timerState.isCompleted) }

    // Auto-hide controls after 4 seconds when timer is running and unpaused
    LaunchedEffect(controlsVisible, timerState.isRunning, timerState.isPaused, timerState.isCompleted) {
        if (controlsVisible && timerState.isRunning && !timerState.isPaused && !timerState.isCompleted) {
            delay(4000L)
            controlsVisible = false
        }
    }

    // Always reveal controls when timer pauses
    LaunchedEffect(timerState.isPaused) {
        if (timerState.isPaused) {
            controlsVisible = true
        }
    }

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

    val isDark = isTactileThemeDark()

    val phaseColor = when (timerState.phase) {
        TimerPhase.WARMUP -> PhaseWarmupColor
        TimerPhase.FOCUS -> timerState.colorScheme.primaryColor
        TimerPhase.BREAK -> if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
        TimerPhase.COOLDOWN -> PhaseCooldownColor
        TimerPhase.COMPLETED -> PhaseCompletedColor
    }

    // Playful spring bounce when starting a session and on phase transitions
    val phaseBounceScale = remember { Animatable(0.86f) }
    LaunchedEffect(Unit) {
        // Initial session start entrance bounce
        phaseBounceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    LaunchedEffect(timerState.phase) {
        // Playful elastic bounce between focus, break, warmup, and cooldown
        phaseBounceScale.snapTo(0.90f)
        phaseBounceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    // Playful checkmark pop for completion
    val checkmarkPopScale = remember { Animatable(0f) }
    LaunchedEffect(timerState.isCompleted) {
        if (timerState.isCompleted) {
            checkmarkPopScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else {
            checkmarkPopScale.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!timerState.isCompleted) {
                    controlsVisible = !controlsVisible
                }
            }
            .systemBarsPadding()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight
        val ringDiameter = if (isLandscape) {
            min(screenHeight - 12.dp, screenWidth * 0.85f)
        } else {
            min(screenWidth - 12.dp, screenHeight * 0.64f)
        }

        // Layer 0: Motion Animated Timer Background
        AnimatedTimerBackground(
            animation = effectiveAnimation,
            phase = timerState.phase,
            progress = timerState.progress,
            colorScheme = timerState.colorScheme,
            isDark = isDark,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 0.5: Playful Phase & Start Ripple Wave
        PhaseRippleEffect(
            triggerKey = timerState.phase,
            color = phaseColor
        )

        // Layer 1: Background Circular Countdown Ring (Soft & Muted with playful bounce)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(phaseBounceScale.value),
            contentAlignment = Alignment.Center
        ) {
            CircularTimerRing(
                modifier = Modifier.size(ringDiameter),
                progress = timerState.progress,
                phase = timerState.phase,
                remainingSeconds = timerState.remainingSecondsInPhase,
                accentColor = timerState.colorScheme.primaryColor,
                colorScheme = timerState.colorScheme,
                strokeWidth = if (isLandscape) 14.dp else 16.dp,
                isMuted = true
            )
        }

        // Layer 2: Center Typography & Information (Scales playfully with phase changes)
        if (isLandscape) {
            // Landscape Top Metadata Bar (Compact so center digits have 100% vertical freedom)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = timerState.phase,
                    transitionSpec = {
                        (slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)))
                            .togetherWith(
                                slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(180)) +
                                fadeOut(animationSpec = tween(150))
                            )
                    },
                    label = "phase_badge_landscape"
                ) { targetPhase ->
                    TactileSunkenWell(
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(targetPhase.titleRes).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = phaseColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }

                if (!timerState.isCompleted) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(
                            R.string.round_indicator,
                            timerState.currentRound,
                            timerState.totalRounds
                        ) + if (timerState.routineName.isNotBlank()) "  •  ${timerState.routineName}" else "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }

            // Landscape Giant Countdown Digits (Fills the entire display height & width)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.then(
                        if (timerState.isCompleted) Modifier.scale(checkmarkPopScale.value)
                        else Modifier.scale(phaseBounceScale.value)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    AutoSizingTimerText(
                        text = if (timerState.isCompleted) "✓" else timerState.formattedRemainingTime,
                        color = if (timerState.isCompleted) PhaseCompletedColor else MaterialTheme.colorScheme.onBackground,
                        maxFontSize = (screenHeight.value * 0.88f).sp
                    )
                }
            }
        } else {
            // Portrait Layout: Stacked Phase Badge, Digits & Round Info
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    // Phase Badge with playful spring transition
                    AnimatedContent(
                        targetState = timerState.phase,
                        transitionSpec = {
                            (slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)))
                                .togetherWith(
                                    slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(180)) +
                                    fadeOut(animationSpec = tween(150))
                                )
                        },
                        label = "phase_badge_portrait"
                    ) { targetPhase ->
                        TactileSunkenWell(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = stringResource(targetPhase.titleRes).uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = phaseColor,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Giant Countdown Digits filling the screen (with playful checkmark pop)
                    Box(
                        modifier = Modifier.then(
                            if (timerState.isCompleted) Modifier.scale(checkmarkPopScale.value)
                            else Modifier.scale(phaseBounceScale.value)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoSizingTimerText(
                            text = if (timerState.isCompleted) "✓" else timerState.formattedRemainingTime,
                            color = if (timerState.isCompleted) PhaseCompletedColor else MaterialTheme.colorScheme.onBackground,
                            maxFontSize = (screenWidth.value * 0.42f).sp
                        )
                    }

                    // Round Indicator / Subtitle
                    if (!timerState.isCompleted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.round_indicator,
                                    timerState.currentRound,
                                    timerState.totalRounds
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (timerState.routineName.isNotBlank()) {
                                Text(
                                    text = "  •  " + timerState.routineName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Layer 3: Controls Overlay (Shown on click, hidden when running)
        // Top App Bar
        AnimatedVisibility(
            visible = (controlsVisible || timerState.isPaused) && !timerState.isCompleted,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = if (isLandscape) 6.dp else 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TactileCard(
                        modifier = Modifier
                            .size(if (isLandscape) 42.dp else 46.dp)
                            .clickable {
                                if (timerState.isRunning && !timerState.isCompleted) {
                                    showExitDialog = true
                                } else {
                                    onExitTimer()
                                }
                            },
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
                    if (isLandscape && timerState.routineName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = timerState.routineName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (!isLandscape) {
                    Text(
                        text = timerState.routineName.ifBlank { stringResource(R.string.app_short_name) },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.size(46.dp))
                }
            }
        }

        // Bottom Controls Row
        AnimatedVisibility(
            visible = (controlsVisible || timerState.isPaused) && !timerState.isCompleted,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val buttonPadding = if (isLandscape) 6.dp else 24.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = buttonPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tactile Restart Button
                TactileCard(
                    modifier = Modifier
                        .size(if (isLandscape) 46.dp else 60.dp)
                        .clickable {
                            timerEngine.restart()
                            PaceTimerService.start(context)
                            controlsVisible = false
                        },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Replay,
                            contentDescription = stringResource(R.string.btn_restart),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (isLandscape) 22.dp else 28.dp)
                        )
                    }
                }

                // Tactile Play/Pause Pill Button with Brand Gradient
                // Tactile Play/Pause Pill Button with Dynamic Routine / Metallic Gradient
                val playPauseBrush = if (timerState.phase == TimerPhase.BREAK) {
                    if (isDark) {
                        Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                    }
                } else {
                    Brush.linearGradient(listOf(timerState.colorScheme.primaryColor, timerState.colorScheme.secondaryColor))
                }

                TactilePillButton(
                    modifier = Modifier
                        .size(if (isLandscape) 64.dp else 86.dp)
                        .clickable {
                            val wasPaused = timerState.isPaused
                            timerEngine.togglePauseResume()
                            if (wasPaused) {
                                controlsVisible = false
                            }
                        },
                    shape = CircleShape,
                    elevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(playPauseBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (timerState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (timerState.isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause),
                            tint = Color.White,
                            modifier = Modifier.size(if (isLandscape) 34.dp else 46.dp)
                        )
                    }
                }

                // Tactile Skip Button
                TactileCard(
                    modifier = Modifier
                        .size(if (isLandscape) 46.dp else 60.dp)
                        .clickable {
                            timerEngine.skipPhase()
                        },
                    shape = CircleShape,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.FastForward,
                            contentDescription = stringResource(R.string.btn_skip),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (isLandscape) 22.dp else 28.dp)
                        )
                    }
                }
            }
        }

        // Completed State Actions with celebratory Confetti & playful slide-up
        if (timerState.isCompleted) {
            ConfettiEffect(modifier = Modifier.fillMaxSize())

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                AnimatedVisibility(
                    visible = timerState.isCompleted,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(animationSpec = tween(300))
                ) {
                    CompletedActions(
                        totalRounds = timerState.totalRounds,
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
private fun AutoSizingTimerText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxFontSize: TextUnit,
    minFontSize: TextUnit = 40.sp
) {
    var fontSizeValue by remember(text.length, maxFontSize) { mutableFloatStateOf(maxFontSize.value) }
    var readyToDraw by remember(text.length, maxFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = fontSizeValue.sp,
            lineHeight = fontSizeValue.sp,
            fontFamily = FontFamily.Default,
            letterSpacing = (-1).sp,
            shadow = Shadow(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                offset = Offset(0f, 2f),
                blurRadius = 6f
            )
        ),
        fontWeight = FontWeight.Black,
        color = color,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth || textLayoutResult.didOverflowHeight) {
                val nextSize = fontSizeValue * 0.93f
                if (nextSize >= minFontSize.value) {
                    fontSizeValue = nextSize
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
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

