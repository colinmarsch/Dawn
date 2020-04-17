package me.colinmarsch.dawn

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.DELAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createNotificationChannel(context)
        if (intent.getBooleanExtra("ringing", true)) {
            val alarmIntent = Intent(context, AlarmActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, alarmIntent, FLAG_UPDATE_CURRENT)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText("Time to get up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIF_ID, builder.build())
            }
        } else {
            val stayInIntent = Intent(context, InAppActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, stayInIntent, FLAG_UPDATE_CURRENT)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                .setContentTitle("Dawn")
                .setContentText("You have 30 seconds to click here or your streak will be broken!")
                // TODO(colinmarsch) not sure about the category and priority here
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis() + 30000L)
                .setExtras(Bundle()) // TODO(colinmarsch) figure out a better way to solve issue of mExtras being null
                .setUsesChronometer(true)
                .setChronometerCountDown(true)

            with(NotificationManagerCompat.from(context)) {
                cancel(DELAY_NOTIF_ID)
                notify(STAY_NOTIF_ID, builder.build())
            }
        }
    }
}