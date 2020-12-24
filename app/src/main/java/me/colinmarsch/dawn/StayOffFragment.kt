package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import java.util.*

class StayOffFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var stayOffTimePicker: NumberPicker
    private lateinit var toggleButton: Button
    private var pendingIntent: PendingIntent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.stayoff_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleButton = view.findViewById(R.id.alarm_set_toggle)
        toggleButton.setOnClickListener {
            onSetAlarmClicked()
        }

        alarmManager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sharedPrefs = view.context.getSharedPreferences(
            getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )

        stayOffTimePicker = view.findViewById(R.id.stayOffTimePicker)
        stayOffTimePicker.maxValue = 60
        stayOffTimePicker.minValue = 1
        val stayOffMinutes =
            (sharedPrefs.getLong(getString(R.string.STAY_OFF_KEY), 5) / 60000L).toInt()
        stayOffTimePicker.value = stayOffMinutes
    }

    private fun onSetAlarmClicked() {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("CASE", "ALARM")
        val mainIntent = Intent(context, MainActivity::class.java)
        val sharedPrefs = requireContext().getSharedPreferences(
            getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )
        val calendar = Calendar.getInstance()
        calendar.set(
            Calendar.HOUR_OF_DAY,
            sharedPrefs.getInt(getString(R.string.saved_hour_key), 0)
        )
        calendar.set(
            Calendar.MINUTE,
            sharedPrefs.getInt(getString(R.string.saved_minute_key), 0)
        )
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

        val pendingMainIntent =
            PendingIntent.getActivity(context, NotificationHelper.TIME_NOTIF_ID, mainIntent, 0)
        NotificationHelper.createNotificationChannel(requireContext())
        val builder = NotificationCompat.Builder(requireContext(), NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif)
            .setColor(Color.argb(1, 221, 182, 57))
            .setContentTitle("Alarm Set")
            .setContentText(contentText)
            .setContentIntent(pendingMainIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOngoing(true)
            // TODO(colinmarsch) need a real dismiss icon here
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", pendingDismissIntent)

        with(NotificationManagerCompat.from(requireContext())) {
            notify(NotificationHelper.TIME_NOTIF_ID, builder.build())
        }

        pendingIntent =
            PendingIntent.getBroadcast(context, NotificationHelper.ALARM_ID, alarmIntent, 0)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                calendar.timeInMillis,
                pendingIntent
            ), pendingIntent
        )
        Log.d("DAWN", "Started the alarm for $calendar")

        setStayOffTime()
        finishAffinity(requireActivity())
    }

    private fun Calendar.hourText() =
        if (get(Calendar.HOUR_OF_DAY) == 12 || get(Calendar.HOUR_OF_DAY) == 0) {
            "12"
        } else {
            (get(Calendar.HOUR_OF_DAY) % 12).toString()
        }

    private fun Calendar.minuteText(): String {
        val minuteNum = get(Calendar.MINUTE)
        return if (minuteNum < 10) {
            "0$minuteNum"
        } else {
            minuteNum.toString()
        }
    }

    private fun setStayOffTime() {
        val time = stayOffTimePicker.value * 60000L
        val sharedPrefs = requireContext().getSharedPreferences(
            getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )
        with(sharedPrefs.edit()) {
            putLong(getString(R.string.STAY_OFF_KEY), time)
            apply()
        }
    }

    companion object {
        const val TAG = "STAY_OFF_FRAGMENT_TAG"
    }
}