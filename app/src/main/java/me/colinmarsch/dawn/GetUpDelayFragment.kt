package me.colinmarsch.dawn

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.Fragment

class GetUpDelayFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var getUpDelayPicker: NumberPicker
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.getup_delay_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmManager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sharedPrefs =
            view.context.getSharedPreferences(
                getString(R.string.shared_prefs_name),
                Context.MODE_PRIVATE
            )

        getUpDelayPicker = view.findViewById(R.id.getUpDelayPicker)
        getUpDelayPicker.maxValue = 60
        getUpDelayPicker.minValue = 1
        val getUpDelayMinutes =
            (sharedPrefs.getLong(getString(R.string.GET_UP_DELAY_KEY), 5) / 60000L).toInt()
        getUpDelayPicker.value = getUpDelayMinutes

        nextButton = view.findViewById(R.id.set_getup_delay)
        nextButton.setOnClickListener {
            // TODO(colinmarsch): move this to the activity
            val intent = Intent(view.context, StayOffActivity::class.java)
            setGetUpDelayTime(sharedPrefs)
            startActivity(intent)
        }
    }

    private fun setGetUpDelayTime(sharedPrefs: SharedPreferences) {
        val time = getUpDelayPicker.value * 60000L
        with(sharedPrefs.edit()) {
            putLong(getString(R.string.GET_UP_DELAY_KEY), time)
            apply()
        }
    }
}