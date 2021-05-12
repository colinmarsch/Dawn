package me.colinmarsch.dawn.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import me.colinmarsch.dawn.*
import me.colinmarsch.dawn.NotificationHelper.Companion.ALARM_CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.ALARM_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.BREATHER_CANCEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.Channel.ALARM
import me.colinmarsch.dawn.NotificationHelper.Companion.Channel.STREAK
import me.colinmarsch.dawn.NotificationHelper.Companion.DELAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NO_IMPACT_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.SNOOZE_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STREAK_CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.SUCCESS_STREAK_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.TIME_NOTIF_ID
import me.colinmarsch.dawn.utils.cancelAlarm
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefsHelper = RealPreferencesHelper(context)
        when (intent.getStringExtra("CASE")) {
            "ALARM" -> {
                NotificationHelper.createNotificationChannel(context, ALARM)
                MediaHandler.startAlarm(context)

                val alarmIntent = Intent(context, AlarmActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, alarmIntent, FLAG_UPDATE_CURRENT)

                val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("CASE", "STOP")
                }
                val pendingStopIntent =
                    PendingIntent.getBroadcast(context, 0, stopIntent, FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Good Morning!")
                    .setContentText("Tap to stop the alarm")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setFullScreenIntent(pendingIntent, true)
                    .addAction(R.drawable.ic_launcher_foreground, "Stop Alarm", pendingStopIntent)

                with(NotificationManagerCompat.from(context)) {
                    cancel(TIME_NOTIF_ID)
                    cancel(SNOOZE_NOTIF_ID)
                    notify(NOTIF_ID, builder.build())
                }
            }
            "STAY" -> {
                NotificationHelper.createNotificationChannel(context, ALARM)
                val breatherTime = System.currentTimeMillis() + 30000L
                val stayInIntent = Intent(context, InAppActivity::class.java).apply {
                    putExtra("WHEN_TIME", breatherTime)
                }
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, stayInIntent, FLAG_UPDATE_CURRENT)
                val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Get back to Dawn")
                    .setContentText("30 seconds left to get back to Dawn!")
                    .setContentIntent(pendingIntent)
                    .setWhen(breatherTime)
                    .setExtras(Bundle()) // TODO(colinmarsch) figure out a better way to solve issue of mExtras being null
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)

                with(NotificationManagerCompat.from(context)) {
                    cancel(DELAY_NOTIF_ID)
                    notify(STAY_NOTIF_ID, builder.build())
                }

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val breatherIntent = Intent(context, AlarmReceiver::class.java)
                breatherIntent.putExtra("CASE", "BREATHER")
                val alarmPendingIntent =
                    PendingIntent.getBroadcast(context, BREATHER_CANCEL_ID, breatherIntent, 0)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    breatherTime + 5000L,
                    alarmPendingIntent
                )
            }
            "STREAK" -> {
                NotificationHelper.createNotificationChannel(context, STREAK)
                val builder = NotificationCompat.Builder(context, STREAK_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Congrats!")
                    .setContentText("Congrats! You can now use your phone!")

                val currentStreak = prefsHelper.getStreak()
                val c: Date = Calendar.getInstance().time
                val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val currentDay: String = df.format(c)
                val newSuccessfulDaysSet = prefsHelper.getSuccessfulDays()
                val newFailedDaysSet = prefsHelper.getFailedDays()
                if (!newFailedDaysSet.contains(currentDay)
                    && !newSuccessfulDaysSet.contains(currentDay)
                ) {
                    with(NotificationManagerCompat.from(context)) {
                        notify(SUCCESS_STREAK_NOTIF_ID, builder.build())
                    }

                    prefsHelper.recordSuccessfulDay()
                    prefsHelper.setStreak(currentStreak + 1)
                } else {
                    val noImpactBuilder = NotificationCompat.Builder(context, STREAK_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setColor(Color.argb(1, 221, 182, 57))
                        .setContentTitle("You already used Dawn today")
                        .setContentText("Only the first alarm per day counts!")

                    with(NotificationManagerCompat.from(context)) {
                        notify(NO_IMPACT_NOTIF_ID, noImpactBuilder.build())
                    }
                }
            }
            "BREATHER" -> {
                NotificationHelper.createNotificationChannel(context, STREAK)
                val builder = NotificationCompat.Builder(context, STREAK_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Day missed")
                    .setContentText("You didn't open Dawn!")

                with(NotificationManagerCompat.from(context)) {
                    cancel(STAY_NOTIF_ID)
                    notify(NotificationHelper.BROKE_STREAK_NOTIF_ID, builder.build())
                }

                prefsHelper.recordFailedDay()
                prefsHelper.setStreak(0)
            }
            "STOP" -> {
                NotificationHelper.createNotificationChannel(context, STREAK)
                MediaHandler.stopAlarm(context)

                val getUpDelayTime = prefsHelper.getGetUpDelayTime()
                val whenTime = System.currentTimeMillis() + getUpDelayTime
                val inAppIntent = Intent(context, InAppActivity::class.java).apply {
                    putExtra("WHEN_TIME", whenTime)
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
                val contentIntent = PendingIntent.getActivity(
                    context,
                    NotificationHelper.STAY_IN_APP_ID,
                    inAppIntent,
                    FLAG_UPDATE_CURRENT
                )
                val builder = NotificationCompat.Builder(context, STREAK_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Countdown to get up!")
                    .setContentText("You can use your phone for a bit!")
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setOngoing(true)
                    .setContentIntent(contentIntent)
                    .setExtras(Bundle()) // TODO(colinmarsch) figure out a better way to solve issue of mExtras being null
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(whenTime)

                with(NotificationManagerCompat.from(context)) {
                    cancel(NOTIF_ID)
                    notify(DELAY_NOTIF_ID, builder.build())
                }

                val stayIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("CASE", "STAY")
                }
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NotificationHelper.STAY_ALARM_ID,
                    stayIntent,
                    0
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    whenTime - 30000L,
                    pendingIntent
                )

                startActivity(context, inAppIntent, null)
            }
            "SNOOZE" -> {
                NotificationHelper.createNotificationChannel(context, ALARM)
                MediaHandler.stopAlarm(context)

                val snoozeDuration = prefsHelper.getSnoozeDuration()
                val whenTime = System.currentTimeMillis() + snoozeDuration
                val alarmDismissIntent = Intent(context, AlarmReceiver::class.java).also {
                    it.putExtra("CASE", "DISMISS")
                }
                val pendingDismissIntent = PendingIntent.getBroadcast(
                    context,
                    NotificationHelper.DISMISS_ALARM_ID,
                    alarmDismissIntent,
                    0
                )

                val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Alarm Snoozed")
                    .setContentText("Alarm snoozed for ${snoozeDuration / 60000L} minutes")
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setOngoing(true)
                    .setExtras(Bundle()) // TODO(colinmarsch) figure out a better way to solve issue of mExtras being null
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(whenTime)
                    .addAction(R.drawable.ic_launcher_foreground, "Dismiss", pendingDismissIntent)

                with(NotificationManagerCompat.from(context)) {
                    cancel(NOTIF_ID)
                    notify(SNOOZE_NOTIF_ID, builder.build())
                }

                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                alarmIntent.putExtra("CASE", "ALARM")
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, alarmIntent, 0)
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(whenTime, pendingIntent),
                    pendingIntent
                )
            }
            "DISMISS" -> {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancelAlarm(context, ALARM_ID)
                with(NotificationManagerCompat.from(context)) {
                    cancel(TIME_NOTIF_ID)
                    cancel(SNOOZE_NOTIF_ID)
                }
            }
        }
    }
}