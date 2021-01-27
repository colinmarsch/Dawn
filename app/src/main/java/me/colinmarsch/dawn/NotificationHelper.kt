package me.colinmarsch.dawn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NotificationHelper {
    companion object {

        enum class Channel(val id: String, val title: String) {
            ALARM(ALARM_CHANNEL_ID, "Alarms"),
            STREAK(STREAK_CHANNEL_ID, "Streak alerts")
        }

        const val ALARM_CHANNEL_ID = "dawn-alarm-channel"
        const val STREAK_CHANNEL_ID = "dawn-streak-channel"
        const val NOTIF_ID = 1
        const val ALARM_ID = 2
        const val TIME_NOTIF_ID = 3
        const val DELAY_NOTIF_ID = 4
        const val STAY_NOTIF_ID = 5
        const val STAY_ALARM_ID = 6
        const val STAY_IN_APP_ID = 7
        const val BROKE_STREAK_NOTIF_ID = 8
        const val SUCCESS_STREAK_ALARM_ID = 9
        const val SUCCESS_STREAK_NOTIF_ID = 10
        const val BREATHER_CANCEL_ID = 11
        const val DISMISS_ALARM_ID = 12
        const val NO_IMPACT_NOTIF_ID = 13
        const val SNOOZE_NOTIF_ID = 14

        fun createNotificationChannel(context: Context, channel: Channel) {
            // NotificationChannel class is new and not in the support library, so version restrict here
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notificationChannel = NotificationChannel(channel.id, channel.title, importance)
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
}