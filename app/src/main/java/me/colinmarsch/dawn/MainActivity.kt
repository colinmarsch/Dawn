package me.colinmarsch.dawn

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager.*
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: TimePicker
    private lateinit var nextButton: Button
    private lateinit var ringtoneButton: Button
    private lateinit var ringtoneLabel: TextView
    private lateinit var ringtoneVolume: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timePicker = findViewById(R.id.alarm_time_picker)
        nextButton = findViewById(R.id.choose_alarm_time_button)
        nextButton.setOnClickListener {
            val intent = Intent(this, SetTimerActivity::class.java).also {
                it.putExtra("hour", timePicker.hour)
                it.putExtra("minute", timePicker.minute)
                val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
                println(ringtoneVolume.progress)
                with(sharedPrefs.edit()) {
                    putInt(getString(R.string.saved_volume_key), ringtoneVolume.progress)
                    apply()
                }
            }
            startActivity(intent)
        }
        ringtoneButton = findViewById(R.id.choose_ringtone_button)
        ringtoneButton.setOnClickListener {
            val ringtoneIntent = Intent(ACTION_RINGTONE_PICKER)
            ringtoneIntent.putExtra(EXTRA_RINGTONE_TITLE, "Select ringtone for alarm:")
            ringtoneIntent.putExtra(EXTRA_RINGTONE_SHOW_SILENT, false)
            ringtoneIntent.putExtra(EXTRA_RINGTONE_SHOW_DEFAULT, true)
            ringtoneIntent.putExtra(EXTRA_RINGTONE_TYPE, TYPE_ALARM)
            startActivityForResult(ringtoneIntent, 1)
        }
        ringtoneLabel = findViewById(R.id.ringtone_label)

        ringtoneVolume = findViewById(R.id.ringtone_volume_slider)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                val ringtonePath = uri.toString()
                val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString(getString(R.string.saved_ringtone_key), ringtonePath)
                    apply()
                }
            }
            val ringtone = getRingtone(this, uri)
            val title = ringtone.getTitle(this)
            ringtoneLabel.text = title
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
        ringtoneVolume.progress = sharedPrefs.getInt(getString(R.string.saved_volume_key), 0)

        if (alarmManager.nextAlarmClock != null) {
            val nextAlarm = alarmManager.nextAlarmClock
            val time = Calendar.getInstance()
            time.timeInMillis = nextAlarm.triggerTime
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        }
    }
}
