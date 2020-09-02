package me.colinmarsch.dawn

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity

class GetUpDelayActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var getUpDelayPicker: NumberPicker
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.getup_delay_layout)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sharedPrefs =
            getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)

        getUpDelayPicker = findViewById(R.id.getUpDelayPicker)
        getUpDelayPicker.maxValue = 60
        getUpDelayPicker.minValue = 1
        val getUpDelayMinutes =
            (sharedPrefs.getLong(getString(R.string.GET_UP_DELAY_KEY), 5) / 60000L).toInt()
        getUpDelayPicker.value = getUpDelayMinutes

        nextButton = findViewById(R.id.set_getup_delay)
        nextButton.setOnClickListener {
            val intent = Intent(this, StayOffActivity::class.java).also {
                setGetUpDelayTime()
            }
            startActivity(intent)
        }
    }

    private fun setGetUpDelayTime() {
        val time = getUpDelayPicker.value * 60000L
        val sharedPrefs =
            getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong(getString(R.string.GET_UP_DELAY_KEY), time)
            apply()
        }
    }
}