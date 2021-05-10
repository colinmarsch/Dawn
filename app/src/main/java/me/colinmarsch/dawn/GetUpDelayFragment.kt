package me.colinmarsch.dawn

import android.os.Bundle
import android.view.HapticFeedbackConstants.CLOCK_TICK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.Fragment

class GetUpDelayFragment : Fragment() {
    private lateinit var getUpDelayPicker: NumberPicker
    private lateinit var nextButton: Button

    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.getup_delay_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.app_name)

        prefsHelper = RealPreferencesHelper(view.context)

        getUpDelayPicker = view.findViewById(R.id.getUpDelayPicker)
        getUpDelayPicker.maxValue = 60
        getUpDelayPicker.minValue = 1
        val getUpDelayMinutes = (prefsHelper.getGetUpDelayTime() / 60000L).toInt()
        getUpDelayPicker.value = getUpDelayMinutes
        getUpDelayPicker.setOnValueChangedListener { picker, _, _ ->
            picker.performHapticFeedback(CLOCK_TICK)
        }

        nextButton = view.findViewById(R.id.set_getup_delay)
        nextButton.setOnClickListener {
            setGetUpDelayTime()
            (requireActivity() as MainActivity).transitionToStayOff()
        }
    }

    private fun setGetUpDelayTime() = prefsHelper.setGetUpDelayTime(getUpDelayPicker.value * 60000L)

    companion object {
        const val TAG = "GET_UP_FRAGMENT_TAG"
    }
}