package ch.simibu.pace

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ch.simibu.pace.data.SettingsRepository
import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.Routine
import ch.simibu.pace.ui.components.PaceBottomBar
import ch.simibu.pace.ui.components.PaceScreen
import ch.simibu.pace.ui.screens.QuickStartScreen
import ch.simibu.pace.ui.screens.RoutinesScreen
import ch.simibu.pace.ui.screens.SettingsScreen
import ch.simibu.pace.ui.screens.TimerScreen
import ch.simibu.pace.ui.theme.PaceAmigoTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        setContent {
            val settingsRepo = PaceApplication.instance.settingsRepository
            val timerEngine = PaceApplication.instance.timerEngine

            val themeMode by settingsRepo.themeMode.collectAsState()
            val timerState by timerEngine.state.collectAsState()

            val isDark = when (themeMode) {
                SettingsRepository.THEME_LIGHT -> false
                SettingsRepository.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            val activeAccent = if (timerState.isRunning || timerState.isCompleted) {
                timerState.colorScheme
            } else {
                ColorSchemeOption.PACE
            }

            PaceAmigoTheme(
                darkTheme = isDark,
                accentOption = activeAccent
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PaceAmigoApp(
                        isTimerActive = timerState.isRunning || timerState.isCompleted
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun PaceAmigoApp(isTimerActive: Boolean) {
    var currentTab by remember { mutableStateOf(PaceScreen.QUICK_START.route) }
    var userInTimerView by remember { mutableStateOf(false) }

    val shouldShowFullscreenTimer = isTimerActive || userInTimerView

    AnimatedContent(
        targetState = shouldShowFullscreenTimer,
        transitionSpec = {
            if (targetState) {
                // Playful bouncy pop into timer screen
                (fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.85f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ).togetherWith(
                    fadeOut(animationSpec = tween(220)) +
                        scaleOut(targetScale = 1.05f, animationSpec = tween(220))
                )
            } else {
                // Smooth playful scale down on exit
                (fadeIn(animationSpec = tween(280)) +
                    scaleIn(initialScale = 1.05f, animationSpec = tween(280))
                ).togetherWith(
                    fadeOut(animationSpec = tween(220)) +
                        scaleOut(targetScale = 0.88f, animationSpec = tween(220))
                )
            }
        },
        label = "screen_transition"
    ) { inTimerScreen ->
        if (inTimerScreen) {
            TimerScreen(
                onExitTimer = {
                    userInTimerView = false
                }
            )
        } else {
            Scaffold(
                bottomBar = {
                    PaceBottomBar(
                        currentRoute = currentTab,
                        onNavigate = { route -> currentTab = route }
                    )
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        PaceScreen.QUICK_START.route -> {
                            QuickStartScreen(
                                onStartSession = {
                                    userInTimerView = true
                                }
                            )
                        }
                        PaceScreen.ROUTINES.route -> {
                            RoutinesScreen(
                                onStartRoutine = {
                                    userInTimerView = true
                                }
                            )
                        }
                        PaceScreen.SETTINGS.route -> {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
