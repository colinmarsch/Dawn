package me.colinmarsch.dawn

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.Calendar

class MainFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: TimePicker
    private lateinit var nextButton: Button
    private lateinit var ringtoneButton: Button
    private lateinit var ringtoneLabel: TextView
    private lateinit var ringtoneVolume: SeekBar

    private lateinit var sharedPrefs: SharedPreferences

    private var previewPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = view.context.getSharedPreferences(
            getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )

        timePicker = view.findViewById(R.id.alarm_time_picker)
        timePicker.setOnTimeChangedListener { _, hour, minute ->
            with(sharedPrefs.edit()) {
                putInt(getString(R.string.saved_hour_key), hour)
                putInt(getString(R.string.saved_minute_key), minute)
                apply()
            }
        }
        nextButton = view.findViewById(R.id.choose_alarm_time_button)
        nextButton.setOnClickListener {
            if (!MediaHandler.validRingtoneSet(view.context)) {
                Toast.makeText(view.context, getString(R.string.switch_ringtone), Toast.LENGTH_LONG)
                    .show()
            } else {
                (requireActivity() as MainActivity).transitionToGetUpDelay()
            }
        }
        ringtoneButton = view.findViewById(R.id.choose_ringtone_button)
        ringtoneButton.setOnClickListener {
            val ringtoneIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            ringtoneIntent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_TITLE,
                "Select ringtone for alarm:"
            )
            ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            startActivityForResult(ringtoneIntent, 1)
        }
        ringtoneLabel = view.findViewById(R.id.ringtone_label)
        val currentRingtoneTitle =
            sharedPrefs.getString(getString(R.string.saved_ringtone_title_key), "Default")
        ringtoneLabel.text = getString(R.string.current_ringtone, currentRingtoneTitle)

        ringtoneVolume = view.findViewById(R.id.ringtone_volume_slider)
        ringtoneVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                with(sharedPrefs.edit()) {
                    putInt(getString(R.string.saved_volume_key), ringtoneVolume.progress)
                    apply()
                }
                if (!previewPlaying && seekBar.progress != 0) {
                    MediaHandler.startAlarm(view.context)
                    previewPlaying = true
                    seekBar.postDelayed({
                        MediaHandler.stopAlarm()
                        previewPlaying = false
                    }, 2000)
                }
            }
        })

        alarmManager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onResume() {
        super.onResume()
        ringtoneVolume.progress = sharedPrefs.getInt(getString(R.string.saved_volume_key), 0)

        if (alarmManager.nextAlarmClock != null) {
            val nextAlarm = alarmManager.nextAlarmClock
            val time = Calendar.getInstance()
            time.timeInMillis = nextAlarm.triggerTime
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        } else {
            val currentTime = Calendar.getInstance()
            val hour = sharedPrefs.getInt(
                getString(R.string.saved_hour_key),
                currentTime.get(Calendar.HOUR_OF_DAY)
            )
            val minute = sharedPrefs.getInt(
                getString(R.string.saved_minute_key),
                currentTime.get(Calendar.MINUTE)
            )
            timePicker.hour = hour
            timePicker.minute = minute
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                val ringtonePath = uri.toString()
                with(sharedPrefs.edit()) {
                    putString(getString(R.string.saved_ringtone_key), ringtonePath)
                    apply()
                }
            }
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone.getTitle(context)
            ringtoneLabel.text = getString(R.string.current_ringtone, title)

            with(sharedPrefs.edit()) {
                putString(getString(R.string.saved_ringtone_title_key), title)
                apply()
            }
        }
    }
}