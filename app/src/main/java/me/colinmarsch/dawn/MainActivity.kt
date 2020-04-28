package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
        // TODO(colinmarsch) set the time picker to display the set alarm time if there is an alarm set
        toggleButton.isChecked = alarmManager.nextAlarmClock != null
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

            val hour = if (calendar.get(Calendar.HOUR_OF_DAY) == 12) {
                12
            } else {
                calendar.get(Calendar.HOUR_OF_DAY) % 12
            }
            val minute = calendar.get(Calendar.MINUTE)
            val amPM = calendar.get(Calendar.AM_PM) == Calendar.AM
            // TODO(colinmarsch) need to fix the displaying if there are single digit minutes
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
}
