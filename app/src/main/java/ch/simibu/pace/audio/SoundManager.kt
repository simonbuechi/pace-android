package ch.simibu.pace.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import ch.simibu.pace.R
import ch.simibu.pace.model.SoundScheme

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    private var isLoaded = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        val rawSounds = listOf(
            R.raw.beep,
            R.raw.digital_pulse,
            R.raw.gentle_chime,
            R.raw.marimba_pop,
            R.raw.singing_bowl,
            R.raw.temple_bell,
            R.raw.zen_gong
        )

        for (resId in rawSounds) {
            val soundId = soundPool.load(context, resId, 1)
            soundMap[resId] = soundId
        }
        isLoaded = true
    }

    fun playSchemeSound(scheme: SoundScheme, volume: Float = 1.0f) {
        val rawResId = scheme.rawResId ?: return
        val soundId = soundMap[rawResId] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun playCountdownTick(volume: Float = 0.8f) {
        val beepId = soundMap[R.raw.beep] ?: return
        soundPool.play(beepId, volume, volume, 2, 0, 1.2f)
    }

    fun playCompletionSound(volume: Float = 1.0f) {
        val chimeId = soundMap[R.raw.zen_gong] ?: soundMap[R.raw.gentle_chime] ?: return
        soundPool.play(chimeId, volume, volume, 3, 0, 1.0f)
    }

    fun vibrateShort() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(80)
            }
        }
    }

    fun vibratePhaseTransition() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 150, 100, 250)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                it.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 150, 100, 250), -1)
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}
