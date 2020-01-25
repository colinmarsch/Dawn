package me.colinmarsch.dawn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, AlarmActivity::class.java)
        i.addFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
        println("DAWN ran the start activity code")
    }
}