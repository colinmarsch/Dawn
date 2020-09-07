package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.BREATHER_CANCEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.BROKE_STREAK_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NO_IMPACT_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_ALARM_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.SUCCESS_STREAK_ALARM_ID
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class InAppActivity : AppCompatActivity() {

    private lateinit var countDownTimerText: TextView
    private lateinit var streakLabel: TextView
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_app_activity)
        sharedPref =
            getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)

        cancelAlarmAndNotif()
        cancelBreatherAlarm()

        countDownTimerText = findViewById(R.id.countdownTimeText)
        startCountdown()

        val streakVal = sharedPref.getInt(getString(R.string.saved_streak_key), 0)

        streakLabel = findViewById(R.id.streakLabel)
        streakLabel.text = String.format(getString(R.string.in_app_streak_label), streakVal)
    }

    override fun onPause() {
        // Don't want to break the streak if the activity is paused due to locking the phone
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val timeIsComplete = countDownTimerText.text == getString(R.string.in_app_complete)
        if (powerManager.isInteractive && !timeIsComplete) {
            NotificationHelper.createNotificationChannel(applicationContext)
            val brokenBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText("You left Dawn and broke your streak!")

            val newSuccessfulDaysSet: HashSet<String> = HashSet(
                sharedPref.getStringSet(
                    getString(R.string.successful_days_key),
                    HashSet<String>()
                )
            )
            val newFailedDaysSet: HashSet<String> = HashSet(
                sharedPref.getStringSet(
                    getString(R.string.failed_days_key),
                    HashSet<String>()
                )
            )
            val c: Date = Calendar.getInstance().time
            val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val currentDay: String = df.format(c)
            if (!newFailedDaysSet.contains(currentDay) && !newSuccessfulDaysSet.contains(currentDay)) {
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(BROKE_STREAK_NOTIF_ID, brokenBuilder.build())
                }

                newFailedDaysSet.add(currentDay)
                with(sharedPref.edit()) {
                    putStringSet(getString(R.string.failed_days_key), newFailedDaysSet)
                    putInt(getString(R.string.saved_streak_key), 0)
                    apply()
                }
            } else {
                val noImpactBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                    .setContentTitle("Dawn")
                    .setContentText("Only the first alarm per day counts for streaks!")

                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(NO_IMPACT_NOTIF_ID, noImpactBuilder.build())
                }
            }

            // Cancel the alarm from the countdown
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val successStreakIntent = Intent(this, AlarmReceiver::class.java)
            successStreakIntent.putExtra("CASE", "STREAK")
            val pendingIntent =
                PendingIntent.getBroadcast(this, SUCCESS_STREAK_ALARM_ID, successStreakIntent, 0)
            alarmManager.cancel(pendingIntent)
        }
        super.onPause()
    }

    private fun startCountdown() {
        val totalTime = sharedPref.getLong(getString(R.string.STAY_OFF_KEY), 300000L)
        val interval = 1000L

        val whenTime = System.currentTimeMillis() + totalTime
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val successStreakIntent = Intent(this, AlarmReceiver::class.java)
        successStreakIntent.putExtra("CASE", "STREAK")
        val pendingIntent =
            PendingIntent.getBroadcast(this, SUCCESS_STREAK_ALARM_ID, successStreakIntent, 0)
        alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, whenTime, pendingIntent)

        object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000L
                val secondsNum = (millisUntilFinished % 60000L) / 1000L
                val seconds = if (secondsNum < 10) {
                    "0$secondsNum"
                } else {
                    secondsNum.toString()
                }
                countDownTimerText.text =
                    "$minutes:$seconds" // TODO(colinmarsch) fix this string behaviour
            }

            override fun onFinish() {
                countDownTimerText.text = getString(R.string.in_app_complete)
            }
        }.start()
    }

    private fun cancelAlarmAndNotif() {
        val inAppIntent = Intent(this, AlarmReceiver::class.java)
        val dupIntent = PendingIntent.getBroadcast(this, STAY_ALARM_ID, inAppIntent, 0)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).also {
            it.setExactAndAllowWhileIdle(RTC_WAKEUP, System.currentTimeMillis() + 1000L, dupIntent)
            it.cancel(dupIntent)
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(STAY_NOTIF_ID)
        }
    }

    private fun cancelBreatherAlarm() {
        val breatherIntent = Intent(this, AlarmReceiver::class.java)
        val dupIntent = PendingIntent.getBroadcast(this, BREATHER_CANCEL_ID, breatherIntent, 0)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).also {
            it.setExactAndAllowWhileIdle(RTC_WAKEUP, System.currentTimeMillis() + 1000L, dupIntent)
            it.cancel(dupIntent)
        }
    }
}