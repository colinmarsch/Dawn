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
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

class StreaksDialog(context: Context) : Dialog(context) {

    private lateinit var leftArrow: View
    private lateinit var rightArrow: View
    private lateinit var monthText: TextView
    private lateinit var calendar: CalendarView
    private lateinit var button: Button

    private lateinit var firstDay: TextView
    private lateinit var secondDay: TextView
    private lateinit var thirdDay: TextView
    private lateinit var fourthDay: TextView
    private lateinit var fifthDay: TextView
    private lateinit var sixthDay: TextView
    private lateinit var seventhDay: TextView

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

        val prefsHelper = RealPreferencesHelper(context)
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
            val firstDayOfWeek = prefsHelper.getFirstDayOfWeek()
            setup(firstMonth, lastMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)
        }

        button = findViewById(R.id.calendar_close_dialog)
        button.setOnClickListener { dismiss() }

        setUpDaysOfWeekHeaders(prefsHelper)
    }

    // TODO(colinmarsch) is there a different way I could do this?
    private fun setUpDaysOfWeekHeaders(prefsHelper: PreferencesHelper) {
        firstDay = findViewById(R.id.firstDay)
        secondDay = findViewById(R.id.secondDay)
        thirdDay = findViewById(R.id.thirdDay)
        fourthDay = findViewById(R.id.fourthDay)
        fifthDay = findViewById(R.id.fifthDay)
        sixthDay = findViewById(R.id.sixthDay)
        seventhDay = findViewById(R.id.seventhDay)

        val days = mutableListOf(
            context.getString(R.string.day_monday),
            context.getString(R.string.day_tuesday),
            context.getString(R.string.day_wednesday),
            context.getString(R.string.day_thursday),
            context.getString(R.string.day_friday),
            context.getString(R.string.day_saturday),
            context.getString(R.string.day_sunday)
        )

        when (prefsHelper.getFirstDayOfWeek()) {
            DayOfWeek.SUNDAY -> {
                val sun = days.removeAt(6)
                days.add(0, sun)
            }
            DayOfWeek.SATURDAY -> {
                val sun = days.removeAt(6)
                val sat = days.removeAt(5)
                days.add(0, sun)
                days.add(0, sat)
            }
            else -> {
                // nothing needs to be done
            }
        }

        firstDay.text = days[0]
        secondDay.text = days[1]
        thirdDay.text = days[2]
        fourthDay.text = days[3]
        fifthDay.text = days[4]
        sixthDay.text = days[5]
        seventhDay.text = days[6]
    }
}