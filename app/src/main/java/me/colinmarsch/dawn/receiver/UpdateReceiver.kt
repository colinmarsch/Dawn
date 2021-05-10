package me.colinmarsch.dawn.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MY_PACKAGE_REPLACED
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.MainActivity
import me.colinmarsch.dawn.NotificationHelper
import me.colinmarsch.dawn.R
import me.colinmarsch.dawn.RealPreferencesHelper
import me.colinmarsch.dawn.utils.hourText
import me.colinmarsch.dawn.utils.minuteText
import me.colinmarsch.dawn.utils.setSavedTime
import java.util.*

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MY_PACKAGE_REPLACED) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.nextAlarmClock?.showIntent?.creatorPackage == context.packageName) {
                val mainIntent = Intent(context, MainActivity::class.java)
                val calendar = Calendar.getInstance()
                val prefsHelper = RealPreferencesHelper(context)
                calendar.setSavedTime(prefsHelper)
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

                val alarmDismissIntent = Intent(context, AlarmReceiver::class.java).also {
                    it.putExtra("CASE", "DISMISS")
                }
                val pendingDismissIntent = PendingIntent.getBroadcast(
                    context,
                    NotificationHelper.DISMISS_ALARM_ID,
                    alarmDismissIntent,
                    0
                )

                val pendingMainIntent = PendingIntent.getActivity(
                    context,
                    NotificationHelper.TIME_NOTIF_ID,
                    mainIntent,
                    0
                )
                NotificationHelper.createNotificationChannel(
                    context,
                    NotificationHelper.Companion.Channel.ALARM
                )
                val builder =
                    NotificationCompat.Builder(context, NotificationHelper.ALARM_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setColor(Color.argb(1, 221, 182, 57))
                        .setContentTitle("Alarm Set")
                        .setContentText(contentText)
                        .setContentIntent(pendingMainIntent)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setOngoing(true)
                        // TODO(colinmarsch) need a real dismiss icon here
                        .addAction(
                            R.drawable.ic_launcher_foreground,
                            "Dismiss",
                            pendingDismissIntent
                        )

                with(NotificationManagerCompat.from(context)) {
                    notify(NotificationHelper.TIME_NOTIF_ID, builder.build())
                }
            }
        }
    }
}