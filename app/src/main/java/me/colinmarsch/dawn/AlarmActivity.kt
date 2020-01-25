package me.colinmarsch.dawn

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_ALARM
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException


class AlarmActivity: AppCompatActivity() {
    private lateinit var stopAlarmButton: Button
    private lateinit var audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var userVolume = 0 // TODO(colinmarsch) is there a better way to handle this?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)
        Log.d("DAWN", "Started the AlarmActivity")
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        userVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        // Retrieve default ringtone file URI
        val myUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)

        // Set up MediaPlayer asynchronously
        mediaPlayer = MediaPlayer()
        class Listener : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 2,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
        }
        mediaPlayer?.apply {
            setOnPreparedListener(Listener())
            val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_ALARM).build()
            setAudioAttributes(audioAttributes)
            try {
                setDataSource(applicationContext, myUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            prepareAsync()
        }
    }
}
