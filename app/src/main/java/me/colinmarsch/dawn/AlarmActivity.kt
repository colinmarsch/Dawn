package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener {
            MediaHandler.stopAlarm()

            NotificationHelper.createNotificationChannel(applicationContext)
            val stayIntent = Intent(this, AlarmReceiver::class.java)
            stayIntent.putExtra("CASE", "STAY")
            val inAppIntent = Intent(this, InAppActivity::class.java)
            val contentIntent = PendingIntent.getActivity(this, STAY_IN_APP_ID, inAppIntent, 0)
            val sharedPref = getSharedPreferences(
                getString(R.string.shared_prefs_name),
                Context.MODE_PRIVATE
            )
            val getUpDelayTime = sharedPref.getLong(getString(R.string.GET_UP_DELAY_KEY), 600000L)
            val whenTime = System.currentTimeMillis() + getUpDelayTime
            val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText("Tap here if you want to get up before the countdown!")
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
        MediaHandler.startAlarm(this)
    }
}
