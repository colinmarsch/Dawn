package me.colinmarsch.dawn

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants.CLOCK_TICK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {
    private lateinit var timePicker: TimePicker
    private lateinit var nextButton: Button
    private lateinit var ringtoneButton: Button
    private lateinit var ringtoneLabel: TextView
    private lateinit var ringtoneVolume: SeekBar

    private lateinit var prefsHelper: PreferencesHelper

    private var previewPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.app_name)

        prefsHelper = RealPreferencesHelper(view.context)

        val savedHour = prefsHelper.getSavedHour()
        val savedMinute = prefsHelper.getSavedMinute()
        timePicker = view.findViewById(R.id.alarm_time_picker)
        timePicker.hour = savedHour
        timePicker.minute = savedMinute
        timePicker.setOnTimeChangedListener { timePicker, hour, minute ->
            timePicker.performHapticFeedback(CLOCK_TICK)
            prefsHelper.setSavedHour(hour)
            prefsHelper.setSavedMinute(minute)
        }
        nextButton = view.findViewById(R.id.choose_alarm_time_button)
        nextButton.setOnClickListener {
            if (shouldConfirmTimeSelection) {
                ConfirmationDialog(view.context).apply {
                    setOwnerActivity(requireActivity())
                    show()
                }
            } else if (!MediaHandler.validRingtoneSet(view.context)) {
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
        val currentRingtoneTitle = prefsHelper.getRingtoneTitle()
        ringtoneLabel.text = getString(R.string.current_ringtone, currentRingtoneTitle)

        ringtoneVolume = view.findViewById(R.id.ringtone_volume_slider)
        ringtoneVolume.progress = prefsHelper.getVolume()
        ringtoneVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefsHelper.setVolume(ringtoneVolume.progress)
                if (MediaHandler.validRingtoneSet(view.context)) {
                    if (!previewPlaying && seekBar.progress != 0) {
                        MediaHandler.startAlarm(view.context)
                        previewPlaying = true
                        seekBar.postDelayed({
                            MediaHandler.stopAlarm(requireContext())
                            previewPlaying = false
                        }, 2000)
                    }
                } else {
                    Toast.makeText(
                        view.context,
                        getString(R.string.switch_ringtone),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                val ringtonePath = uri.toString()
                prefsHelper.setRingtonePath(ringtonePath)
            }
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone.getTitle(context)
            ringtoneLabel.text = getString(R.string.current_ringtone, title)

            prefsHelper.setRingtoneTitle(title)
        }
    }

    private val shouldConfirmTimeSelection: Boolean
        get() = prefsHelper.getSavedHour() > 13 && prefsHelper.getPmConfirmationSetting()
}