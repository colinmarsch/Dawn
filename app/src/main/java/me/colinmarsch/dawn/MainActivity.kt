package me.colinmarsch.dawn

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager.*
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    private lateinit var sharedPrefs: SharedPreferences

    private var previewPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences(
            getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )

        timePicker = findViewById(R.id.alarm_time_picker)
        nextButton = findViewById(R.id.choose_alarm_time_button)
        nextButton.setOnClickListener {
            val intent = Intent(this, GetUpDelayActivity::class.java).also {
                with(sharedPrefs.edit()) {
                    putInt(getString(R.string.saved_hour_key), timePicker.hour)
                    putInt(getString(R.string.saved_minute_key), timePicker.minute)
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
        val currentRingtoneTitle =
            sharedPrefs.getString(getString(R.string.saved_ringtone_title_key), "Default")
        ringtoneLabel.text = getString(R.string.current_ringtone, currentRingtoneTitle)

        ringtoneVolume = findViewById(R.id.ringtone_volume_slider)
        ringtoneVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                with(sharedPrefs.edit()) {
                    putInt(getString(R.string.saved_volume_key), ringtoneVolume.progress)
                    apply()
                }
                if (!previewPlaying && seekBar.progress != 0) {
                    MediaHandler.startAlarm(this@MainActivity)
                    previewPlaying = true
                    seekBar.postDelayed({
                        MediaHandler.stopAlarm()
                        previewPlaying = false
                    }, 2000)
                }
            }
        })

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                val ringtonePath = uri.toString()
                with(sharedPrefs.edit()) {
                    putString(getString(R.string.saved_ringtone_key), ringtonePath)
                    apply()
                }
            }
            val ringtone = getRingtone(this, uri)
            val title = ringtone.getTitle(this)
            ringtoneLabel.text = getString(R.string.current_ringtone, title)

            with(sharedPrefs.edit()) {
                putString(getString(R.string.saved_ringtone_title_key), title)
                apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ringtoneVolume.progress = sharedPrefs.getInt(getString(R.string.saved_volume_key), 0)

        if (alarmManager.nextAlarmClock != null) {
            val nextAlarm = alarmManager.nextAlarmClock
            val time = Calendar.getInstance()
            time.timeInMillis = nextAlarm.triggerTime
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        } else {
            val currentTime = Calendar.getInstance()
            timePicker.hour = sharedPrefs.getInt(
                getString(R.string.saved_hour_key),
                currentTime.get(Calendar.HOUR_OF_DAY)
            )
            timePicker.minute = sharedPrefs.getInt(
                getString(R.string.saved_minute_key),
                currentTime.get(Calendar.MINUTE)
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.streaks_menu_option -> openStreaksScreen()
        else -> super.onOptionsItemSelected(item)
    }

    private fun openStreaksScreen(): Boolean {
        val intent = Intent(this, StreaksActivity::class.java)
        startActivity(intent)
        return true
    }
}
