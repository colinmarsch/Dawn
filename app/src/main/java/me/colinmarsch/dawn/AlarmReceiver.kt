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
import me.colinmarsch.dawn.NotificationHelper.Companion.SUCCESS_STREAK_NOTIF_ID


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createNotificationChannel(context)
        when (intent.getStringExtra("CASE")) {
            "ALARM" -> {
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
            }
            "STAY" -> {
                val stayInIntent = Intent(context, InAppActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, stayInIntent, FLAG_UPDATE_CURRENT)
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                    .setContentTitle("Dawn")
                    // TODO(colinmarsch) the message is cutoff here
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
                
                // TODO(colinmarsch) set a 30 second alarm here to break the streak if you don't go in the app in time
            }
            "STREAK" -> {
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO(colinmarsch) update the icon
                    .setContentTitle("Dawn")
                    .setContentText("Congrats on keeping your streak! You can now use your phone!")

                with(NotificationManagerCompat.from(context)) {
                    notify(SUCCESS_STREAK_NOTIF_ID, builder.build())
                }

                val sharedPrefs =
                    context.getSharedPreferences(context.getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
                val currentStreak = sharedPrefs.getInt(context.getString(R.string.saved_streak_key), 0)
                with(sharedPrefs.edit()) {
                    putInt(context.getString(R.string.saved_streak_key), currentStreak + 1)
                    apply()
                }
            }
        }
    }
}