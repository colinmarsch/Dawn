package me.colinmarsch.dawn

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import java.io.IOException

object MediaHandler {
    private var mediaPlayer: MediaPlayer? = null
    private var userVolume = 0 // TODO(colinmarsch) is there a better way to handle this?

    fun startAlarm(context: Context) {
        if (mediaPlayer?.isPlaying == true) return

        val audioManager = getSystemService(context, AudioManager::class.java) as AudioManager
        userVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        // Retrieve default ringtone file URI
        val prefsHelper = RealPreferencesHelper(context)
        val myUri = prefsHelper.getRingtonePath().toUri()

        // Set up MediaPlayer asynchronously
        mediaPlayer = mediaPlayer ?: MediaPlayer()
        class Listener : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
                val savedVolume = prefsHelper.getVolume()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * (savedVolume / 7.toDouble())).toInt(),
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
        }
        mediaPlayer?.apply {
            setOnPreparedListener(Listener())
            val audioAttributes =
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            setAudioAttributes(audioAttributes)
            try {
                setDataSource(context, myUri)
                prepareAsync()
            } catch (e: IOException) {
                Toast.makeText(context, context.getString(R.string.switch_ringtone), LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }
    }

    fun stopAlarm(context: Context) {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        val audioManager = getSystemService(context, AudioManager::class.java) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            userVolume,
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    fun validRingtoneSet(context: Context): Boolean {
        // Retrieve default ringtone file URI
        val prefsHelper = RealPreferencesHelper(context)
        val testUri = prefsHelper.getRingtonePath().toUri()

        try {
            val testPlayer = MediaPlayer()
            testPlayer.setDataSource(context, testUri)
            testPlayer.release()
        } catch (e: Exception) {
            return false
        }

        return true
    }
}