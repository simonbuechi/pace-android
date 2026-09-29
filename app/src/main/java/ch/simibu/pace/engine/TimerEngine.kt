package ch.simibu.pace.engine

import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.SoundScheme
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.model.TimerState
import ch.simibu.pace.data.RoutineLogRepository
import ch.simibu.pace.model.RoutineLogEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class TimerEvent {
    data class CountdownTick(val secondsLeft: Int) : TimerEvent()
    data class PhaseTransition(
        val newPhase: TimerPhase,
        val round: Int,
        val scheme: SoundScheme,
        val repeats: Int = 1
    ) : TimerEvent()
    object Completed : TimerEvent()
}

class TimerEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val routineLogRepository: RoutineLogRepository? = null
) {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private var tickerJob: Job? = null

    // Session configuration
    private var currentRoutineId: String? = null
    private var sessionTotalDurationSeconds: Int = 0
    private var warmupSeconds: Int = 0
    private var focusSeconds: Int = 45
    private var breakSeconds: Int = 15
    private var cooldownSeconds: Int = 0
    private var totalRounds: Int = 8
    private var currentFocusSoundScheme: SoundScheme = SoundScheme.BELL
    private var currentFocusSoundRepeats: Int = 1
    private var currentBreakSoundScheme: SoundScheme = SoundScheme.CHIME
    private var currentBreakSoundRepeats: Int = 1
    private var currentColorScheme: ColorSchemeOption = ColorSchemeOption.PACE
    private var sessionTitle: String = ""

    fun startRoutine(
        routine: Routine,
        focusSound: SoundScheme = routine.soundScheme,
        focusRepeats: Int = 1,
        breakSound: SoundScheme = routine.soundScheme,
        breakRepeats: Int = 1
    ) {
        startSession(
            title = routine.name,
            routineId = routine.id,
            totalDurationSec = routine.totalDurationSeconds,
            focus = routine.totalFocusSeconds,
            rest = routine.totalBreakSeconds,
            rounds = routine.iterations,
            warmup = routine.totalWarmupSeconds,
            cooldown = routine.totalCooldownSeconds,
            focusSound = focusSound,
            focusRepeats = focusRepeats,
            breakSound = breakSound,
            breakRepeats = breakRepeats,
            color = routine.colorScheme
        )
    }

    fun startQuickTimer(
        focusSec: Int,
        breakSec: Int,
        rounds: Int,
        warmupSec: Int = 0,
        cooldownSec: Int = 0,
        focusSound: SoundScheme = SoundScheme.BELL,
        focusRepeats: Int = 1,
        breakSound: SoundScheme = SoundScheme.CHIME,
        breakRepeats: Int = 1,
        color: ColorSchemeOption = ColorSchemeOption.PACE
    ) {
        startSession(
            title = "Quick Session",
            routineId = null,
            totalDurationSec = (warmupSec + cooldownSec + ((focusSec + breakSec) * rounds)),
            focus = focusSec,
            rest = breakSec,
            rounds = rounds,
            warmup = warmupSec,
            cooldown = cooldownSec,
            focusSound = focusSound,
            focusRepeats = focusRepeats,
            breakSound = breakSound,
            breakRepeats = breakRepeats,
            color = color
        )
    }

    private fun startSession(
        title: String,
        routineId: String? = null,
        totalDurationSec: Int = 0,
        focus: Int,
        rest: Int,
        rounds: Int,
        warmup: Int,
        cooldown: Int = 0,
        focusSound: SoundScheme,
        focusRepeats: Int = 1,
        breakSound: SoundScheme,
        breakRepeats: Int = 1,
        color: ColorSchemeOption
    ) {
        tickerJob?.cancel()

        sessionTitle = title
        currentRoutineId = routineId
        sessionTotalDurationSeconds = if (totalDurationSec > 0) totalDurationSec else (warmup + cooldown + ((focus + rest) * rounds))
        focusSeconds = focus.coerceAtLeast(1)
        breakSeconds = rest.coerceAtLeast(1)
        totalRounds = rounds.coerceAtLeast(1)
        warmupSeconds = warmup.coerceAtLeast(0)
        cooldownSeconds = cooldown.coerceAtLeast(0)
        currentFocusSoundScheme = focusSound
        currentFocusSoundRepeats = focusRepeats.coerceIn(1, 5)
        currentBreakSoundScheme = breakSound
        currentBreakSoundRepeats = breakRepeats.coerceIn(1, 5)
        currentColorScheme = color

        val initialPhase = if (warmupSeconds > 0) TimerPhase.WARMUP else TimerPhase.FOCUS
        val initialDuration = if (warmupSeconds > 0) warmupSeconds else focusSeconds
        val initialSound = currentFocusSoundScheme
        val initialRepeats = if (warmupSeconds > 0) 1 else currentFocusSoundRepeats

        _state.value = TimerState(
            phase = initialPhase,
            currentRound = 1,
            totalRounds = totalRounds,
            remainingSecondsInPhase = initialDuration,
            totalSecondsInPhase = initialDuration,
            isRunning = true,
            isPaused = false,
            isCompleted = false,
            routineName = sessionTitle,
            soundScheme = currentFocusSoundScheme,
            colorScheme = currentColorScheme
        )

        _events.tryEmit(TimerEvent.PhaseTransition(initialPhase, 1, initialSound, initialRepeats))
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(dispatcher) {
            while (isActive) {
                delay(1000L)
                tick()
            }
        }
    }

    internal fun performTick() {
        tick()
    }

    private fun tick() {
        val current = _state.value
        if (!current.isRunning || current.isPaused || current.isCompleted) return

        val remaining = current.remainingSecondsInPhase - 1

        if (remaining in 1..3) {
            _events.tryEmit(TimerEvent.CountdownTick(remaining))
        }

        if (remaining <= 0) {
            advancePhase()
        } else {
            _state.value = current.copy(remainingSecondsInPhase = remaining)
        }
    }

    fun pause() {
        if (_state.value.isRunning && !_state.value.isPaused) {
            _state.value = _state.value.copy(isPaused = true)
        }
    }

    fun resume() {
        if (_state.value.isRunning && _state.value.isPaused) {
            _state.value = _state.value.copy(isPaused = false)
        }
    }

    fun togglePauseResume() {
        if (_state.value.isPaused) resume() else pause()
    }

    fun skipPhase() {
        if (_state.value.isRunning && !_state.value.isCompleted) {
            advancePhase()
        }
    }

    fun stop() {
        tickerJob?.cancel()
        _state.value = _state.value.copy(
            isRunning = false,
            isPaused = false,
            isCompleted = false,
            remainingSecondsInPhase = 0
        )
    }

    fun restart() {
        startSession(
            title = sessionTitle,
            routineId = currentRoutineId,
            totalDurationSec = sessionTotalDurationSeconds,
            focus = focusSeconds,
            rest = breakSeconds,
            rounds = totalRounds,
            warmup = warmupSeconds,
            cooldown = cooldownSeconds,
            focusSound = currentFocusSoundScheme,
            focusRepeats = currentFocusSoundRepeats,
            breakSound = currentBreakSoundScheme,
            breakRepeats = currentBreakSoundRepeats,
            color = currentColorScheme
        )
    }

    private fun advancePhase() {
        val current = _state.value

        when (current.phase) {
            TimerPhase.WARMUP -> {
                // Transition Warmup -> Focus Round 1
                val newState = current.copy(
                    phase = TimerPhase.FOCUS,
                    currentRound = 1,
                    remainingSecondsInPhase = focusSeconds,
                    totalSecondsInPhase = focusSeconds
                )
                _state.value = newState
                _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.FOCUS, 1, currentFocusSoundScheme, currentFocusSoundRepeats))
            }
            TimerPhase.FOCUS -> {
                if (current.currentRound >= current.totalRounds) {
                    if (cooldownSeconds > 0) {
                        // Transition Focus -> Cool-down
                        val newState = current.copy(
                            phase = TimerPhase.COOLDOWN,
                            remainingSecondsInPhase = cooldownSeconds,
                            totalSecondsInPhase = cooldownSeconds
                        )
                        _state.value = newState
                        _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.COOLDOWN, current.totalRounds, currentBreakSoundScheme, currentBreakSoundRepeats))
                    } else {
                        // All rounds complete
                        finishSession()
                    }
                } else {
                    // Transition Focus -> Break
                    val newState = current.copy(
                        phase = TimerPhase.BREAK,
                        remainingSecondsInPhase = breakSeconds,
                        totalSecondsInPhase = breakSeconds
                    )
                    _state.value = newState
                    _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.BREAK, current.currentRound, currentBreakSoundScheme, currentBreakSoundRepeats))
                }
            }
            TimerPhase.BREAK -> {
                // Transition Break -> Next Focus Round
                val nextRound = current.currentRound + 1
                val newState = current.copy(
                    phase = TimerPhase.FOCUS,
                    currentRound = nextRound,
                    remainingSecondsInPhase = focusSeconds,
                    totalSecondsInPhase = focusSeconds
                )
                _state.value = newState
                _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.FOCUS, nextRound, currentFocusSoundScheme, currentFocusSoundRepeats))
            }
            TimerPhase.COOLDOWN -> {
                finishSession()
            }
            TimerPhase.COMPLETED -> {
                // Already completed
            }
        }
    }

    private fun finishSession() {
        tickerJob?.cancel()
        _state.value = _state.value.copy(
            phase = TimerPhase.COMPLETED,
            isRunning = false,
            isPaused = false,
            isCompleted = true,
            remainingSecondsInPhase = 0
        )
        _events.tryEmit(TimerEvent.Completed)

        currentRoutineId?.let { rId ->
            routineLogRepository?.addLog(
                RoutineLogEntry(
                    routineId = rId,
                    routineName = sessionTitle,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = sessionTotalDurationSeconds,
                    roundsCompleted = totalRounds
                )
            )
        }
    }
}
