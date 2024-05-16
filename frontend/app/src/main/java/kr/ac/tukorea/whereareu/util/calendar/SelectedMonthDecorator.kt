package kr.ac.tukorea.whereareu.util.calendar

import android.content.Context
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kr.ac.tukorea.whereareu.R

class SelectedMonthDecorator(val context: Context, val selectedMonth : Int) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.month != selectedMonth
    }
    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray40)))
    }
}