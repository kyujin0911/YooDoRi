package kr.ac.tukorea.whereareu.util.calendar

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kr.ac.tukorea.whereareu.R
import org.threeten.bp.DayOfWeek

class SaturdayDecorator(val context: Context) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        val saturday = day.date.with(DayOfWeek.SATURDAY).dayOfMonth
        return saturday == day.day
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(object: ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)){})
    }
}