package me.colinmarsch.dawn

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import java.io.IOException

object MediaHandler {
    private lateinit var audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var userVolume = 0 // TODO(colinmarsch) is there a better way to handle this?

    fun startAlarm(context: Context) {
        audioManager = getSystemService(context, AudioManager::class.java) as AudioManager
        userVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        // Retrieve default ringtone file URI
        val sharedPrefs =
            context.getSharedPreferences(context.getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
        val defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        val myUri = sharedPrefs.getString(context.getString(R.string.saved_ringtone_key), defaultUri.path)!!.toUri()

        // Set up MediaPlayer asynchronously
        mediaPlayer = mediaPlayer ?: MediaPlayer()
        class Listener : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
                val savedVolume = sharedPrefs.getInt(context.getString(R.string.saved_volume_key), 4)
                println(savedVolume)
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * (savedVolume / 7.toDouble())).toInt(),
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
        }
        mediaPlayer?.apply {
            setOnPreparedListener(Listener())
            val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            setAudioAttributes(audioAttributes)
            try {
                setDataSource(context, myUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            prepareAsync()
        }
    }

    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND)
    }
}