package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.ALARM_ID
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: TimePicker
    private lateinit var toggleButton: SwitchCompat
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timePicker = findViewById(R.id.alarm_time_picker)
        toggleButton = findViewById(R.id.alarm_set_toggle)
        alarmManager =  getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onResume() {
        super.onResume()
        toggleButton.isChecked = alarmManager.nextAlarmClock != null
    }

    fun onToggleClicked(view: View) {
        val toggle = view as SwitchCompat
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        if (toggle.isChecked) {
            val calendar = Calendar.getInstance()
            // TODO(colinmarsch) fix these to not use deprecated ways of getting the time
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.currentHour)
            calendar.set(Calendar.MINUTE, timePicker.currentMinute)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1)
            }
            // TODO(colinmarsch) add an uncloseable notification to the notification drawer saying what time the
            //  alarm is currently set for
            pendingIntent = PendingIntent.getBroadcast(this, ALARM_ID, alarmIntent, 0)
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent), pendingIntent)
            Log.d("DAWN", "Started the alarm for $calendar")
        } else {
            // This intent was made to be able to cancel the alarm after the app has been closed
            // it matches the original intent that was used to create the alarm
            val dupIntent = PendingIntent.getBroadcast(this, ALARM_ID, alarmIntent, 0)
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 1000L, dupIntent),
                dupIntent
            )
            alarmManager.cancel(dupIntent)
        }
    }
}
