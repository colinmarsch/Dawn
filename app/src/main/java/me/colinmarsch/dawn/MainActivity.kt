package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.ALARM_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.TIME_NOTIF_ID
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: TimePicker
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO(colinmarsch) set up the layout here for just setting the alarm time and the ringtone sound
        setContentView(R.layout.activity_main)

        timePicker = findViewById(R.id.alarm_time_picker)
        nextButton = findViewById(R.id.choose_alarm_time_button)
        nextButton.setOnClickListener {
            val intent = Intent(this, SetTimerActivity::class.java).also {
                it.putExtra("hour", timePicker.hour)
                it.putExtra("minute", timePicker.minute)
            }
            startActivity(intent)
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onResume() {
        super.onResume()
        if (alarmManager.nextAlarmClock != null) {
            val nextAlarm = alarmManager.nextAlarmClock
            val time = Calendar.getInstance()
            time.timeInMillis = nextAlarm.triggerTime
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        }
    }
}
