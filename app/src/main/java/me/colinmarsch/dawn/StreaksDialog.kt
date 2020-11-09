package me.colinmarsch.dawn

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.kizitonwose.calendarview.CalendarView
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class StreaksDialog(context: Context) : Dialog(context) {

    private lateinit var calendar: CalendarView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.streaks_layout)

        calendar = findViewById(R.id.calendarView)
        calendar.apply {
            dayBinder = DayBinder
            monthHeaderBinder = MonthHeaderBinder

            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(12)
            val lastMonth = currentMonth.plusMonths(12)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            setup(firstMonth, lastMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)
        }

        button = findViewById(R.id.calendar_close_dialog)
        button.setOnClickListener { dismiss() }
    }
}