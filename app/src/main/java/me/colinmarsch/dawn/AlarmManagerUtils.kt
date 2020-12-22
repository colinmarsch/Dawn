package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

fun AlarmManager.cancelAlarm(context: Context, requestCode: Int) {
    val cancelIntent = Intent(context, AlarmReceiver::class.java)
    val dupIntent = PendingIntent.getBroadcast(context, requestCode, cancelIntent, 0)
    setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis() + 1000L,
        dupIntent
    )
    cancel(dupIntent)
}