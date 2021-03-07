package me.colinmarsch.dawn

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import java.util.*

class ConfirmationDialog(context: Context) : Dialog(context) {

    private lateinit var positiveButton: Button
    private lateinit var negativeButton: Button
    private lateinit var message: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.confirmation_layout)

        positiveButton = findViewById(R.id.positive_button)
        positiveButton.setOnClickListener {
            val activity = ownerActivity as MainActivity
            activity.transitionToGetUpDelay()
            dismiss()
        }

        negativeButton = findViewById(R.id.negative_button)
        negativeButton.setOnClickListener { dismiss() }

        // TODO(colinmarsch) refactor this to a more general place
        val prefsHelper = RealPreferencesHelper(context)
        val calendar = Calendar.getInstance()
        calendar.set(
            Calendar.HOUR_OF_DAY,
            prefsHelper.getSavedHour()
        )
        calendar.set(
            Calendar.MINUTE,
            prefsHelper.getSavedMinute()
        )
        val hour = calendar.hourText()
        val minute = calendar.minuteText()
        val timeText = "$hour:$minute PM"

        message = findViewById(R.id.confirmation_message_text)
        message.text = context.getString(R.string.confirmation_message, timeText)
    }
}