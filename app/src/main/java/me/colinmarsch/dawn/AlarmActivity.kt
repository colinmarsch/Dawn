package me.colinmarsch.dawn

import android.app.PendingIntent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.DELAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.TIME_NOTIF_ID

class AlarmActivity: AppCompatActivity() {
    private lateinit var stopAlarmButton: Button
    private val mediaHandler = MediaHandler()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)
        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener {
            mediaHandler.stopAlarm()
            with(NotificationManagerCompat.from(this)) {
                cancel(NOTIF_ID)
            }
            with(NotificationManagerCompat.from(this)) {
                cancel(TIME_NOTIF_ID)
            }

            NotificationHelper.createNotificationChannel(applicationContext)
            val whenTime = System.currentTimeMillis() + 600000L // TODO(colinmarsch) make this user defined (10 min rn)
            val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText("Just a short time until you need to get out of bed!")
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOngoing(true)
                .setExtras(Bundle()) // not sure why I need this here
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(whenTime)

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(DELAY_NOTIF_ID, builder.build())
            }
            // TODO(colinmarsch) need to start a forest-ish activity when the current time equals the whenTime above
            // TODO(colinmarsch) cancel this notification when the time is elapsed as well

            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mediaHandler.startAlarm(this)
    }
}
