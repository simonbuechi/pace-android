package ch.simibu.pace.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import ch.simibu.pace.MainActivity
import ch.simibu.pace.PaceApplication
import ch.simibu.pace.R
import ch.simibu.pace.engine.TimerEvent
import ch.simibu.pace.model.TimerPhase
import ch.simibu.pace.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PaceTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var stateCollectJob: Job? = null
    private var eventsCollectJob: Job? = null

    private val timerEngine by lazy { PaceApplication.instance.timerEngine }
    private val soundManager by lazy { PaceApplication.instance.soundManager }
    private val settingsRepo by lazy { PaceApplication.instance.settingsRepository }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        observeEvents()
        observeState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startInForeground()
            }
            ACTION_PAUSE -> {
                timerEngine.pause()
            }
            ACTION_RESUME -> {
                timerEngine.resume()
            }
            ACTION_SKIP -> {
                timerEngine.skipPhase()
            }
            ACTION_STOP -> {
                timerEngine.stop()
                stopForegroundAndSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeEvents() {
        eventsCollectJob?.cancel()
        eventsCollectJob = scope.launch {
            timerEngine.events.collect { event ->
                val soundOn = settingsRepo.soundEnabled.value
                val hapticOn = settingsRepo.vibrationEnabled.value

                when (event) {
                    is TimerEvent.CountdownTick -> {
                        if (soundOn) soundManager.playCountdownTick()
                        if (hapticOn) soundManager.vibrateShort()
                    }
                    is TimerEvent.PhaseTransition -> {
                        if (soundOn) soundManager.playSchemeSound(event.scheme, repeats = event.repeats)
                        if (hapticOn) soundManager.vibratePhaseTransition()
                    }
                    is TimerEvent.Completed -> {
                        if (soundOn) soundManager.playCompletionSound()
                        if (hapticOn) soundManager.vibratePhaseTransition()
                    }
                }
            }
        }
    }

    private fun observeState() {
        stateCollectJob?.cancel()
        stateCollectJob = scope.launch {
            timerEngine.state.collectLatest { state ->
                if (state.isRunning || state.isCompleted) {
                    updateNotification(state)
                } else {
                    stopForegroundAndSelf()
                }
            }
        }
    }

    private fun startInForeground() {
        val notification = buildNotification(timerEngine.state.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(state: TimerState) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: TimerState): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val phaseTitle = when (state.phase) {
            TimerPhase.WARMUP -> getString(R.string.timer_phase_warmup)
            TimerPhase.FOCUS -> getString(R.string.timer_phase_focus)
            TimerPhase.BREAK -> getString(R.string.timer_phase_break)
            TimerPhase.COMPLETED -> getString(R.string.timer_phase_complete)
        }

        val title = if (state.isCompleted) {
            getString(R.string.session_finished_title)
        } else {
            "$phaseTitle • ${state.formattedRemainingTime}"
        }

        val contentText = if (state.isCompleted) {
            getString(R.string.session_finished_msg, state.totalRounds)
        } else {
            val roundInfo = getString(R.string.round_indicator, state.currentRound, state.totalRounds)
            if (state.routineName.isNotBlank()) "$roundInfo • ${state.routineName}" else roundInfo
        }

        val builder = NotificationCompat.Builder(this, PaceApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(state.isRunning && !state.isCompleted)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state.isRunning && !state.isCompleted) {
            // Pause / Resume Action
            if (state.isPaused) {
                val resumeIntent = Intent(this, PaceTimerService::class.java).apply { action = ACTION_RESUME }
                val resumePending = PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(android.R.drawable.ic_media_play, getString(R.string.notification_action_resume), resumePending)
            } else {
                val pauseIntent = Intent(this, PaceTimerService::class.java).apply { action = ACTION_PAUSE }
                val pausePending = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(android.R.drawable.ic_media_pause, getString(R.string.notification_action_pause), pausePending)
            }

            // Skip Action
            val skipIntent = Intent(this, PaceTimerService::class.java).apply { action = ACTION_SKIP }
            val skipPending = PendingIntent.getService(this, 3, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_next, getString(R.string.notification_action_skip), skipPending)

            // Stop Action
            val stopIntent = Intent(this, PaceTimerService::class.java).apply { action = ACTION_STOP }
            val stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notification_action_stop), stopPending)
        }

        return builder.build()
    }

    private fun stopForegroundAndSelf() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stateCollectJob?.cancel()
        eventsCollectJob?.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ch.simibu.pace.ACTION_START"
        const val ACTION_PAUSE = "ch.simibu.pace.ACTION_PAUSE"
        const val ACTION_RESUME = "ch.simibu.pace.ACTION_RESUME"
        const val ACTION_SKIP = "ch.simibu.pace.ACTION_SKIP"
        const val ACTION_STOP = "ch.simibu.pace.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, PaceTimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PaceTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
