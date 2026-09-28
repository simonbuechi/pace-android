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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import ch.simibu.pace.ui.screens.RoutineEditorDialog
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
                ColorSchemeOption.CORAL
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

    // Dialog state for "Save as Routine" from quick start
    var pendingSaveRoutineParams by remember { mutableStateOf<RoutineSaveParams?>(null) }

    val shouldShowFullscreenTimer = isTimerActive || userInTimerView

    AnimatedContent(
        targetState = shouldShowFullscreenTimer,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
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
                                },
                                onSaveAsRoutineRequested = { focusMin, focusSec, breakMin, breakSec, rounds, warmupSec ->
                                    pendingSaveRoutineParams = RoutineSaveParams(
                                        focusMin = focusMin,
                                        focusSec = focusSec,
                                        breakMin = breakMin,
                                        breakSec = breakSec,
                                        rounds = rounds,
                                        warmupSec = warmupSec
                                    )
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

            // Save Routine Dialog invoked from Quick Start
            pendingSaveRoutineParams?.let { params ->
                RoutineEditorDialog(
                    defaultFocusMin = params.focusMin,
                    defaultFocusSec = params.focusSec,
                    defaultBreakMin = params.breakMin,
                    defaultBreakSec = params.breakSec,
                    defaultRounds = params.rounds,
                    defaultWarmupSec = params.warmupSec,
                    onDismiss = { pendingSaveRoutineParams = null },
                    onSave = { newRoutine ->
                        PaceApplication.instance.routineRepository.saveRoutine(newRoutine)
                        pendingSaveRoutineParams = null
                        currentTab = PaceScreen.ROUTINES.route
                    }
                )
            }
        }
    }
}

private data class RoutineSaveParams(
    val focusMin: Int,
    val focusSec: Int,
    val breakMin: Int,
    val breakSec: Int,
    val rounds: Int,
    val warmupSec: Int
)
