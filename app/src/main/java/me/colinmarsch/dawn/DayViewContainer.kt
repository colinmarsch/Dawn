package me.colinmarsch.dawn

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.format.TextStyle
import java.util.*

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)
}

class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarMonthText)
}

object DayBinder : DayBinder<DayViewContainer> {
    // Called only when a new container is needed.
    override fun create(view: View) = DayViewContainer(view)

    // Called every time we need to reuse a container.
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()
    }
}

object MonthHeaderBinder : MonthHeaderFooterBinder<MonthHeaderViewContainer> {
    // Called only when a new container is needed.
    override fun create(view: View) = MonthHeaderViewContainer(view)

    // Called every time we need to reuse a container.
    override fun bind(container: MonthHeaderViewContainer, month: CalendarMonth) {
        container.textView.text =
            month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
}