package kr.ac.tukorea.whereareu.util.calendar

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kr.ac.tukorea.whereareu.R
import java.time.DayOfWeek

class TodayDecorator (context: Context): DayViewDecorator {
    private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_circle)
    private var date = CalendarDay.today()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }

    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(drawable!!)
    }
}