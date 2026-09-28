package ch.simibu.pace

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import ch.simibu.pace.audio.SoundManager
import ch.simibu.pace.data.RoutineRepository
import ch.simibu.pace.data.SettingsRepository
import ch.simibu.pace.engine.TimerEngine

class PaceApplication : Application() {

    lateinit var soundManager: SoundManager
        private set
    lateinit var routineRepository: RoutineRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var timerEngine: TimerEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        soundManager = SoundManager(this)
        routineRepository = RoutineRepository(this)
        settingsRepository = SettingsRepository(this)
        timerEngine = TimerEngine()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW // Low ensures silent ongoing updates
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pace_timer_channel"
        lateinit var instance: PaceApplication
            private set
    }
}
