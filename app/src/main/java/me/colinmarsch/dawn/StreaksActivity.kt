package me.colinmarsch.dawn

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity

class StreaksActivity : AppCompatActivity() {

    private lateinit var calendar: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.streaks_layout)

        calendar = findViewById(R.id.streaks_calendar)
        calendar.date = System.currentTimeMillis()
        calendar.setOnDateChangeListener(::handleDateSelectionChange)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun handleDateSelectionChange(view: View, year: Int, month: Int, dayOfMonth: Int) {
        // TODO(colinmarsch): show some extra information below the calendar
    }
}