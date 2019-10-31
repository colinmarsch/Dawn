package me.colinmarsch.dawn

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: TimePicker
    private lateinit var toggleButton: ToggleButton
    private var pendingIntent: PendingIntent? = null

    // TODO(colinmarsch) think about memory leaks here?
    private val inst = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timePicker = findViewById(R.id.alarm_time_picker)
        toggleButton = findViewById(R.id.alarm_set_toggle)
        alarmManager =  getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun onToggleClicked(view: View) {
        val toggle = view as ToggleButton
        if (toggle.isChecked) {
            val calendar = Calendar.getInstance()
            // TODO(colinmarsch) fix these to not use deprecated ways of getting the time
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.currentHour)
            calendar.set(Calendar.MINUTE, timePicker.currentMinute)
            val alarmIntent = Intent(this, AlarmActivity::class.java)
            alarmIntent.putExtra("thing", "something")
//            startActivity(alarmIntent)
            pendingIntent = PendingIntent.getActivity(this, 0, alarmIntent, FLAG_UPDATE_CURRENT)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d("DAWN", "Started the alarm for $calendar")
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }
}
