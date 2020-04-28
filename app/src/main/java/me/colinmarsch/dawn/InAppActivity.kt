package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.BROKE_STREAK_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_IN_APP_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.SUCCESS_STREAK_ALARM_ID

class InAppActivity : AppCompatActivity() {

    lateinit var countDownTimerText: TextView
    lateinit var streakLabel: TextView
    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_app_activity)

        cancelAlarmAndNotif()

        countDownTimerText = findViewById(R.id.countdownTimeText)
        startCountdown()

        sharedPref = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
        val streakVal = sharedPref.getInt(getString(R.string.saved_streak_key), 0)

        streakLabel = findViewById(R.id.streakLabel)
        streakLabel.text = String.format(getString(R.string.in_app_streak_label), streakVal)
    }

    override fun onPause() {
        super.onPause()
        NotificationHelper.createNotificationChannel(applicationContext)
        val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
            .setContentTitle("Dawn")
            .setContentText("You left Dawn and broke your streak!")

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(BROKE_STREAK_NOTIF_ID, builder.build())
        }

        with(sharedPref.edit()) {
            putInt(getString(R.string.saved_streak_key), 0)
            apply()
        }

        // Cancel the alarm from the countdown
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val successStreakIntent = Intent(this, AlarmReceiver::class.java)
        successStreakIntent.putExtra("CASE", "STREAK")
        val pendingIntent = PendingIntent.getBroadcast(this, SUCCESS_STREAK_ALARM_ID, successStreakIntent, 0)
        alarmManager.cancel(pendingIntent)
    }

    private fun startCountdown() {
        val totalTime = 900000L
        val interval = 1000L

        val whenTime = System.currentTimeMillis() + totalTime
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val successStreakIntent = Intent(this, AlarmReceiver::class.java)
        successStreakIntent.putExtra("CASE", "STREAK")
        val pendingIntent = PendingIntent.getBroadcast(this, SUCCESS_STREAK_ALARM_ID, successStreakIntent, 0)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenTime, pendingIntent)

        object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000L
                val seconds = (millisUntilFinished % 60000L) / 1000L
                countDownTimerText.text = "$minutes:$seconds" // TODO(colinmarsch) fix this string behaviour
            }

            override fun onFinish() {
                countDownTimerText.text = getString(R.string.in_app_complete)
            }
        }.start()
    }

    private fun cancelAlarmAndNotif() {
        val inAppIntent = Intent(this, InAppActivity::class.java)
        val dupIntent = PendingIntent.getBroadcast(this, STAY_IN_APP_ID, inAppIntent, 0)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).also {
            it.setAlarmClock(
                AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 1000L, dupIntent),
                dupIntent
            )
            it.cancel(dupIntent)
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(STAY_NOTIF_ID)
        }
    }
}