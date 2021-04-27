package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants.CLOCK_TICK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import me.colinmarsch.dawn.NotificationHelper.Companion.ALARM_CHANNEL_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.Channel.ALARM
import java.util.*

class StayOffFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var stayOffTimePicker: NumberPicker
    private lateinit var toggleButton: Button
    private var pendingIntent: PendingIntent? = null

    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.stayoff_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.app_name)

        toggleButton = view.findViewById(R.id.alarm_set_toggle)
        toggleButton.setOnClickListener {
            onSetAlarmClicked()
        }
        ViewCompat.setTransitionName(toggleButton, "set_alarm_button")

        alarmManager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        prefsHelper = RealPreferencesHelper(view.context)

        stayOffTimePicker = view.findViewById(R.id.stayOffTimePicker)
        stayOffTimePicker.maxValue = 60
        stayOffTimePicker.minValue = 1
        val stayOffMinutes = (prefsHelper.getStayOffTime() / 60000L).toInt()
        stayOffTimePicker.value = stayOffMinutes
        stayOffTimePicker.setOnValueChangedListener { picker, _, _ ->
            picker.performHapticFeedback(CLOCK_TICK)
        }
    }

    private fun onSetAlarmClicked() {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("CASE", "ALARM")
        val mainIntent = Intent(context, MainActivity::class.java)
        val calendar = Calendar.getInstance()
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

        val pendingMainIntent =
            PendingIntent.getActivity(context, NotificationHelper.TIME_NOTIF_ID, mainIntent, 0)
        NotificationHelper.createNotificationChannel(requireContext(), ALARM)
        val builder = NotificationCompat.Builder(requireContext(), ALARM_CHANNEL_ID)
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

        setStayOffTime()
        finishAffinity(requireActivity())
    }

    private fun setStayOffTime() = prefsHelper.setStayOffTime(stayOffTimePicker.value * 60000L)

    companion object {
        const val TAG = "STAY_OFF_FRAGMENT_TAG"
    }
}