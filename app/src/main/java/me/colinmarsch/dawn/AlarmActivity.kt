package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.DELAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_ALARM_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_IN_APP_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.TIME_NOTIF_ID

class AlarmActivity : AppCompatActivity() {
    private lateinit var stopAlarmButton: Button
    private val mediaHandler = MediaHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)
        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener {
            mediaHandler.stopAlarm()

            NotificationHelper.createNotificationChannel(applicationContext)
            val stayIntent = Intent(this, AlarmReceiver::class.java)
            stayIntent.putExtra("ringing", false)
            val inAppIntent = Intent(this, InAppActivity::class.java)
            val contentIntent = PendingIntent.getActivity(this, STAY_IN_APP_ID, inAppIntent, 0)
            val whenTime = System.currentTimeMillis() + 600000L // TODO(colinmarsch) make this user defined (10 min rn)
            val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText("10 minutes until you need to get up! Click here to get up right now!")
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setExtras(Bundle()) // TODO(colinmarsch) figure out a better way to solve issue of mExtras being null
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(whenTime)

            with(NotificationManagerCompat.from(applicationContext)) {
                cancel(NOTIF_ID)
                cancel(TIME_NOTIF_ID)
                notify(DELAY_NOTIF_ID, builder.build())
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(this, STAY_ALARM_ID, stayIntent, 0)
            alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, whenTime, pendingIntent)

            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mediaHandler.startAlarm(this)
    }
}
