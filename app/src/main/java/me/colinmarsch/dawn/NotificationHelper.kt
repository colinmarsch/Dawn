package me.colinmarsch.dawn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NotificationHelper {
    companion object {

        const val CHANNEL_ID = "dawn-notif-channel"
        const val NOTIF_ID = 1
        const val ALARM_ID = 2
        const val TIME_NOTIF_ID = 3
        const val DELAY_NOTIF_ID = 4
        const val STAY_NOTIF_ID = 5
        const val STAY_ALARM_ID = 6
        const val STAY_IN_APP_ID = 7

        fun createNotificationChannel(context: Context) {
            // NotificationChannel class is new and not in the support library, so version restrict here
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.channel_name)
                val descriptionText = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}