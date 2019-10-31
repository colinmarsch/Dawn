package me.colinmarsch.dawn

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
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)
        Log.d("DAWN", "Started the AlarmActivity")
        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener {
            mediaPlayer?.stop()
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
            }
        }
        mediaPlayer?.apply {
            setOnPreparedListener(Listener())
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                setDataSource(applicationContext, myUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            prepareAsync()
        }
    }
}
