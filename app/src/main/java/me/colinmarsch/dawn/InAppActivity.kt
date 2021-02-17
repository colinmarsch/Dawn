package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import me.colinmarsch.dawn.NotificationHelper.Companion.BREATHER_CANCEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.BROKE_STREAK_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.Channel.STREAK
import me.colinmarsch.dawn.NotificationHelper.Companion.DELAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NO_IMPACT_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_ALARM_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STREAK_CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.SUCCESS_STREAK_ALARM_ID

class InAppActivity : AppCompatActivity() {

    private lateinit var countDownTimerText: TextView
    private lateinit var getUpButton: Button
    private lateinit var description: TextView
    private var countdown: CountDownTimer? = null

    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_app_activity)

        prefsHelper = RealPreferencesHelper(this)

        countDownTimerText = findViewById(R.id.countdownTimeText)
        description = findViewById(R.id.description)
        getUpButton = findViewById(R.id.getup_button)
        getUpButton.setOnClickListener {
            startCountdown()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    override fun onPause() {
        // Don't want to break the streak if the activity is paused due to locking the phone
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val timeIsComplete = countDownTimerText.text == getString(R.string.in_app_complete)
        val stillOnScreenTime = description.text == getString(R.string.time_not_up_text)
        if (powerManager.isInteractive && !timeIsComplete && !stillOnScreenTime) {
            NotificationHelper.createNotificationChannel(applicationContext, STREAK)
            val brokenBuilder = NotificationCompat.Builder(this, STREAK_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(Color.argb(1, 221, 182, 57))
                .setContentTitle("Day missed")
                .setContentText("You left Dawn!")

            val newSuccessfulDaysSet = prefsHelper.getSuccessfulDays()
            val newFailedDaysSet = prefsHelper.getFailedDays()
            val c: Date = Calendar.getInstance().time
            val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val currentDay: String = df.format(c)
            if (!newFailedDaysSet.contains(currentDay) && !newSuccessfulDaysSet.contains(currentDay)) {
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(BROKE_STREAK_NOTIF_ID, brokenBuilder.build())
                }

                prefsHelper.recordFailedDay()
                prefsHelper.setStreak(0)
            } else {
                val noImpactBuilder = NotificationCompat.Builder(this, STREAK_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("You already used Dawn today")
                    .setContentText("Only the first alarm per day counts!")

                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(NO_IMPACT_NOTIF_ID, noImpactBuilder.build())
                }
            }

            // Cancel the alarm from the countdown
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancelAlarm(this, SUCCESS_STREAK_ALARM_ID)
        }
        if (powerManager.isInteractive && stillOnScreenTime && countDownTimerText.text.isNotEmpty()) {
            countdown?.cancel()
        }
        super.onPause()
    }

    private fun handleIntent(intent: Intent) {
        val whenTime = intent.getLongExtra("WHEN_TIME", System.currentTimeMillis())
        if (System.currentTimeMillis() < whenTime) {
            // There is still time remaining to use your phone
            getUpButton.visibility = VISIBLE
            description.text = getString(R.string.time_not_up_text)

            val totalTime = whenTime - System.currentTimeMillis()
            countdown?.cancel()
            countdown = object : CountDownTimer(totalTime, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = millisUntilFinished / 60000L
                    val secondsNum = (millisUntilFinished % 60000L) / 1000L
                    val seconds = if (secondsNum < 10) {
                        "0$secondsNum"
                    } else {
                        secondsNum.toString()
                    }
                    countDownTimerText.text = getString(R.string.timer_text, minutes, seconds)
                }

                override fun onFinish() {
                    startCountdown()
                }
            }
            countdown?.start()

        } else {
            // The time to use your phone is up
            startCountdown()
        }
    }

    private fun startCountdown() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancelAlarm(this, BREATHER_CANCEL_ID)
        alarmManager.cancelAlarm(this, STAY_ALARM_ID)
        cancelNotifs()

        getUpButton.visibility = GONE
        description.text = getString(R.string.stay_in_app_text)

        val totalTime = prefsHelper.getStayOffTime()
        val interval = 1000L

        val whenTime = System.currentTimeMillis() + totalTime
        val successStreakIntent = Intent(this, AlarmReceiver::class.java)
        successStreakIntent.putExtra("CASE", "STREAK")
        val pendingIntent =
            PendingIntent.getBroadcast(this, SUCCESS_STREAK_ALARM_ID, successStreakIntent, 0)
        alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, whenTime, pendingIntent)

        countdown?.cancel()
        countdown = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000L
                val secondsNum = (millisUntilFinished % 60000L) / 1000L
                val seconds = if (secondsNum < 10) {
                    "0$secondsNum"
                } else {
                    secondsNum.toString()
                }
                countDownTimerText.text = getString(R.string.timer_text, minutes, seconds)
            }

            override fun onFinish() {
                countDownTimerText.text = getString(R.string.in_app_complete)
            }
        }
        countdown?.start()
    }

    private fun cancelNotifs() {
        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(STAY_NOTIF_ID)
            cancel(DELAY_NOTIF_ID)
        }
    }
}