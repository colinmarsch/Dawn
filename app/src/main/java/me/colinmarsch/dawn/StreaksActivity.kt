package me.colinmarsch.dawn

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendarview.CalendarView
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class StreaksActivity : AppCompatActivity() {

    private lateinit var calendar: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}