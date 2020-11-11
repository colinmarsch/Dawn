package me.colinmarsch.dawn

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.text.SimpleDateFormat
import java.time.ZoneId
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
        val context = container.view.context
        container.view.setBackgroundColor(Color.TRANSPARENT)
        container.textView.text = day.date.dayOfMonth.toString()
        val today =
            Calendar.getInstance().time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        if (day.date == today) {
            container.textView.typeface = Typeface.DEFAULT_BOLD
        } else {
            container.textView.typeface = Typeface.DEFAULT
        }
        if (day.owner == DayOwner.THIS_MONTH) {
            container.textView.setTextColor(getColor(context, R.color.black))
        } else {
            container.textView.setTextColor(getColor(context, R.color.gray))
        }

        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val successfulDaysSet: HashSet<String> = HashSet(
            sharedPrefs.getStringSet(
                context.getString(R.string.successful_days_key),
                HashSet<String>()
            )
        )
        val successToday = successfulDaysSet.find { success ->
            val date = df.parse(success).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            return@find date == day.date
        }

        if (successToday != null) {
            container.view.setBackgroundColor(getColor(context, R.color.green))
        } else {
            val failedDaysSet: HashSet<String> = HashSet(
                sharedPrefs.getStringSet(
                    context.getString(R.string.failed_days_key),
                    HashSet<String>()
                )
            )
            failedDaysSet.find { failed ->
                val date = df.parse(failed).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                return@find date == day.date
            }?.let {
                container.view.setBackgroundColor(getColor(context, R.color.red))
            }
        }
    }
}

object MonthHeaderBinder : MonthHeaderFooterBinder<MonthHeaderViewContainer> {
    // Called only when a new container is needed.
    override fun create(view: View) = MonthHeaderViewContainer(view)

    // Called every time we need to reuse a container.
    override fun bind(container: MonthHeaderViewContainer, month: CalendarMonth) {
        container.textView.text =
            month.yearMonth.month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            ) + " " + month.year
    }
}