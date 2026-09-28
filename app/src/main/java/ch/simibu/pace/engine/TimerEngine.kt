package ch.simibu.pace.engine

import ch.simibu.pace.model.ColorSchemeOption
import ch.simibu.pace.model.Routine
import ch.simibu.pace.model.SoundScheme
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.model.TimerState
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
    data class PhaseTransition(val newPhase: TimerPhase, val round: Int, val scheme: SoundScheme) : TimerEvent()
    object Completed : TimerEvent()
}

class TimerEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private var tickerJob: Job? = null

    // Session configuration
    private var warmupSeconds: Int = 0
    private var focusSeconds: Int = 45
    private var breakSeconds: Int = 15
    private var totalRounds: Int = 8
    private var currentSoundScheme: SoundScheme = SoundScheme.BEEP
    private var currentColorScheme: ColorSchemeOption = ColorSchemeOption.PACE
    private var sessionTitle: String = ""

    fun startRoutine(routine: Routine) {
        startSession(
            title = routine.name,
            focus = routine.totalFocusSeconds,
            rest = routine.totalBreakSeconds,
            rounds = routine.iterations,
            warmup = routine.warmupSeconds,
            sound = routine.soundScheme,
            color = routine.colorScheme
        )
    }

    fun startQuickTimer(
        focusSec: Int,
        breakSec: Int,
        rounds: Int,
        warmupSec: Int = 0,
        sound: SoundScheme = SoundScheme.BEEP,
        color: ColorSchemeOption = ColorSchemeOption.PACE
    ) {
        startSession(
            title = "Quick Session",
            focus = focusSec,
            rest = breakSec,
            rounds = rounds,
            warmup = warmupSec,
            sound = sound,
            color = color
        )
    }

    private fun startSession(
        title: String,
        focus: Int,
        rest: Int,
        rounds: Int,
        warmup: Int,
        sound: SoundScheme,
        color: ColorSchemeOption
    ) {
        tickerJob?.cancel()

        sessionTitle = title
        focusSeconds = focus.coerceAtLeast(1)
        breakSeconds = rest.coerceAtLeast(1)
        totalRounds = rounds.coerceAtLeast(1)
        warmupSeconds = warmup.coerceAtLeast(0)
        currentSoundScheme = sound
        currentColorScheme = color

        val initialPhase = if (warmupSeconds > 0) TimerPhase.WARMUP else TimerPhase.FOCUS
        val initialDuration = if (warmupSeconds > 0) warmupSeconds else focusSeconds

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
            soundScheme = currentSoundScheme,
            colorScheme = currentColorScheme
        )

        _events.tryEmit(TimerEvent.PhaseTransition(initialPhase, 1, currentSoundScheme))
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
            focus = focusSeconds,
            rest = breakSeconds,
            rounds = totalRounds,
            warmup = warmupSeconds,
            sound = currentSoundScheme,
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
                _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.FOCUS, 1, currentSoundScheme))
            }
            TimerPhase.FOCUS -> {
                if (current.currentRound >= current.totalRounds) {
                    // All rounds complete
                    finishSession()
                } else {
                    // Transition Focus -> Break
                    val newState = current.copy(
                        phase = TimerPhase.BREAK,
                        remainingSecondsInPhase = breakSeconds,
                        totalSecondsInPhase = breakSeconds
                    )
                    _state.value = newState
                    _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.BREAK, current.currentRound, currentSoundScheme))
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
                _events.tryEmit(TimerEvent.PhaseTransition(TimerPhase.FOCUS, nextRound, currentSoundScheme))
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
    }
}
