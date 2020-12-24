package me.colinmarsch.dawn

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class StreaksDialog(context: Context) : Dialog(context) {

    private lateinit var leftArrow: View
    private lateinit var rightArrow: View
    private lateinit var monthText: TextView
    private lateinit var calendar: CalendarView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.streaks_layout)

        leftArrow = findViewById(R.id.left_arrow)
        leftArrow.setOnClickListener {
            calendar.findFirstVisibleMonth()?.let {
                calendar.smoothScrollToMonth(it.yearMonth.previous)
            }
        }
        rightArrow = findViewById(R.id.right_arrow)
        rightArrow.setOnClickListener {
            calendar.findFirstVisibleMonth()?.let {
                calendar.smoothScrollToMonth(it.yearMonth.next)
            }
        }
        monthText = findViewById(R.id.calendarMonthText)

        calendar = findViewById(R.id.calendarView)
        calendar.apply {
            dayBinder = DayBinder
            monthScrollListener = { month ->
                monthText.text = resources.getString(
                    R.string.month_title_text, month.yearMonth.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    ), month.year
                )
            }

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