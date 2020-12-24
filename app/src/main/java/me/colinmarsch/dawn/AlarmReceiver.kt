package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import me.colinmarsch.dawn.NotificationHelper.Companion.ALARM_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.BREATHER_CANCEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.DELAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.NO_IMPACT_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.STAY_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.SUCCESS_STREAK_NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.TIME_NOTIF_ID


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createNotificationChannel(context)
        when (intent.getStringExtra("CASE")) {
            "ALARM" -> {
                MediaHandler.startAlarm(context)

                val alarmIntent = Intent(context, AlarmActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, alarmIntent, FLAG_UPDATE_CURRENT)

                val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("CASE", "STOP")
                }
                val pendingStopIntent =
                    PendingIntent.getBroadcast(context, 0, stopIntent, FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
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
                    notify(NOTIF_ID, builder.build())
                }
            }
            "STAY" -> {
                val breatherTime = System.currentTimeMillis() + 30000L
                val stayInIntent = Intent(context, InAppActivity::class.java).apply {
                    putExtra("WHEN_TIME", breatherTime)
                }
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, stayInIntent, FLAG_UPDATE_CURRENT)
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
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
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Congrats!")
                    .setContentText("Congrats on keeping your streak! You can now use your phone!")

                val sharedPrefs =
                    context.getSharedPreferences(
                        context.getString(R.string.shared_prefs_name),
                        Context.MODE_PRIVATE
                    )
                val currentStreak =
                    sharedPrefs.getInt(context.getString(R.string.saved_streak_key), 0)
                val c: Date = Calendar.getInstance().time
                val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val currentDay: String = df.format(c)
                val newSuccessfulDaysSet: HashSet<String> = HashSet(
                    sharedPrefs.getStringSet(
                        context.getString(R.string.successful_days_key),
                        HashSet<String>()
                    )
                )
                val newFailedDaysSet: HashSet<String> = HashSet(
                    sharedPrefs.getStringSet(
                        context.getString(R.string.failed_days_key),
                        HashSet<String>()
                    )
                )
                if (!newFailedDaysSet.contains(currentDay)
                    && !newSuccessfulDaysSet.contains(currentDay)
                ) {
                    with(NotificationManagerCompat.from(context)) {
                        notify(SUCCESS_STREAK_NOTIF_ID, builder.build())
                    }

                    newSuccessfulDaysSet.add(currentDay)
                    with(sharedPrefs.edit()) {
                        putStringSet(
                            context.getString(R.string.successful_days_key),
                            newSuccessfulDaysSet
                        )
                        putInt(context.getString(R.string.saved_streak_key), currentStreak + 1)
                        apply()
                    }
                } else {
                    val noImpactBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setColor(Color.argb(1, 221, 182, 57))
                        .setContentTitle("You already used Dawn today")
                        .setContentText("Only the first alarm per day counts for streaks!")

                    with(NotificationManagerCompat.from(context)) {
                        notify(NO_IMPACT_NOTIF_ID, noImpactBuilder.build())
                    }
                }
            }
            "BREATHER" -> {
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(Color.argb(1, 221, 182, 57))
                    .setContentTitle("Day missed")
                    .setContentText("You didn't open Dawn! You broke your streak!")

                with(NotificationManagerCompat.from(context)) {
                    cancel(STAY_NOTIF_ID)
                    notify(NotificationHelper.BROKE_STREAK_NOTIF_ID, builder.build())
                }
                val sharedPrefs =
                    context.getSharedPreferences(
                        context.getString(R.string.shared_prefs_name),
                        Context.MODE_PRIVATE
                    )

                val newFailedDaysSet: HashSet<String> = HashSet(
                    sharedPrefs.getStringSet(
                        context.getString(R.string.failed_days_key),
                        HashSet<String>()
                    )
                )
                val c: Date = Calendar.getInstance().time
                val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val currentDay: String = df.format(c)
                newFailedDaysSet.add(currentDay)

                with(sharedPrefs.edit()) {
                    putInt(context.getString(R.string.saved_streak_key), 0)
                    putStringSet(
                        context.getString(R.string.failed_days_key),
                        newFailedDaysSet
                    )
                    apply()
                }
            }
            "STOP" -> {
                MediaHandler.stopAlarm()

                val sharedPref = context.getSharedPreferences(
                    context.getString(R.string.shared_prefs_name),
                    Context.MODE_PRIVATE
                )
                val getUpDelayTime =
                    sharedPref.getLong(context.getString(R.string.GET_UP_DELAY_KEY), 600000L)
                val whenTime = System.currentTimeMillis() + getUpDelayTime
                val inAppIntent = Intent(context, InAppActivity::class.java).apply {
                    putExtra("WHEN_TIME", whenTime)
                }
                val contentIntent = PendingIntent.getActivity(
                    context,
                    NotificationHelper.STAY_IN_APP_ID,
                    inAppIntent,
                    FLAG_UPDATE_CURRENT
                )
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
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
            }
            "DISMISS" -> {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancelAlarm(context, ALARM_ID)
                with(NotificationManagerCompat.from(context)) {
                    cancel(TIME_NOTIF_ID)
                }
            }
        }
    }
}