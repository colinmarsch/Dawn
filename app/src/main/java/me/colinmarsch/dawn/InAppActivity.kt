package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_IN_APP_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID

class InAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_app_activity)
        // TODO(colinmarsch) detect when the user leaves this app/activity
        //  display a notification saying they broke their streak

        cancelAlarmAndNotif()
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