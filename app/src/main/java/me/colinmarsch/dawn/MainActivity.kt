package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private lateinit var toggleButton: SwitchCompat
    private lateinit var stayOffTimePicker: NumberPicker
    private lateinit var getUpDelayPicker: NumberPicker
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timePicker = findViewById(R.id.alarm_time_picker)
        toggleButton = findViewById(R.id.alarm_set_toggle)

        stayOffTimePicker = findViewById(R.id.stayOffTimePicker)
        stayOffTimePicker.maxValue = 60
        stayOffTimePicker.minValue = 1
        stayOffTimePicker.value = 5

        getUpDelayPicker = findViewById(R.id.getUpDelayPicker)
        getUpDelayPicker.maxValue = 60
        getUpDelayPicker.minValue = 1
        getUpDelayPicker.value = 5

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onResume() {
        super.onResume()
        if (alarmManager.nextAlarmClock != null) {
            toggleButton.isChecked = true
            val nextAlarm = alarmManager.nextAlarmClock
            val time = Calendar.getInstance()
            time.timeInMillis = nextAlarm.triggerTime
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        }
    }

    fun onToggleClicked(view: View) {
        val toggle = view as SwitchCompat
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra("CASE", "ALARM")
        val mainIntent = Intent(this, MainActivity::class.java)
        if (toggle.isChecked) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1)
            }

            val hour = calendar.hourText()
            val minute = calendar.minuteText()
            val amPM = calendar.get(Calendar.AM_PM) == Calendar.AM
            val contentText = if (amPM) {
                "Alarm set for $hour:$minute AM"
            } else {
                "Alarm set for $hour:$minute PM"
            }

            val pendingMainIntent = PendingIntent.getActivity(this, TIME_NOTIF_ID, mainIntent, 0)
            NotificationHelper.createNotificationChannel(applicationContext)
            val builder = NotificationCompat.Builder(view.context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText(contentText)
                .setContentIntent(pendingMainIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOngoing(true)
            // TODO(colinmarsch) add a notification action here to stop the alarm maybe?

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(TIME_NOTIF_ID, builder.build())
            }

            pendingIntent = PendingIntent.getBroadcast(this, ALARM_ID, alarmIntent, 0)
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent), pendingIntent)
            Log.d("DAWN", "Started the alarm for $calendar")

            setGetUpDelayTime()
            setStayOffTime()
        } else {
            // This intent was made to be able to cancel the alarm after the app has been closed
            // it matches the original intent that was used to create the alarm
            val dupIntent = PendingIntent.getBroadcast(this, ALARM_ID, alarmIntent, 0)
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 1000L, dupIntent),
                dupIntent
            )
            with(NotificationManagerCompat.from(applicationContext)) {
                cancel(TIME_NOTIF_ID)
            }
            alarmManager.cancel(dupIntent)
        }
    }

    private fun Calendar.hourText() =
        if (get(Calendar.HOUR_OF_DAY) == 12 || get(Calendar.HOUR_OF_DAY) == 0) {
            "12"
        } else {
            (get(Calendar.HOUR_OF_DAY) % 12).toString()
        }

    private fun Calendar.minuteText(): String {
        val minuteNum = get(Calendar.MINUTE)
        return if (minuteNum < 10) {
            "0$minuteNum"
        } else {
            minuteNum.toString()
        }
    }

    private fun setStayOffTime() {
        val time = stayOffTimePicker.value * 60000L
        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong(getString(R.string.STAY_OFF_KEY), time)
            apply()
        }
    }

    private fun setGetUpDelayTime() {
        val time = getUpDelayPicker.value * 60000L
        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong(getString(R.string.GET_UP_DELAY_KEY), time)
            apply()
        }
    }
}
