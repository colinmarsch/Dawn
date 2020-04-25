package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_IN_APP_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID

class InAppActivity : AppCompatActivity() {

    lateinit var countDownTimerText: TextView
    lateinit var streakLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_app_activity)
        // TODO(colinmarsch) detect when the user leaves this app/activity
        //  display a notification saying they broke their streak

        cancelAlarmAndNotif()

        countDownTimerText = findViewById(R.id.countdownTimeText)
        startCountdown()

        streakLabel = findViewById(R.id.streakLabel)
        // TODO(colinmarsch) need to set the streak label to be the current streak the user has achieved
        streakLabel.text = String.format(getString(R.string.in_app_streak_label), 1)
    }

    private fun startCountdown() {
        val totalTime = 900000L
        val interval = 1000L
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