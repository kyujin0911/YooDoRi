package kr.ac.tukorea.whereareu.presentation.nok.history

import androidx.fragment.app.viewModels
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.DialogCalendarBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment
import kr.ac.tukorea.whereareu.util.calendar.DayDecorator
import kr.ac.tukorea.whereareu.util.calendar.SaturdayDecorator
import kr.ac.tukorea.whereareu.util.calendar.SelectedMonthDecorator
import kr.ac.tukorea.whereareu.util.calendar.SundayDecorator
import kr.ac.tukorea.whereareu.util.calendar.TodayDecorator

class CalendarDialogFragment: BaseDialogFragment<DialogCalendarBinding>(R.layout.dialog_calendar) {
    private val viewModel: CalendarDialogViewModel by viewModels()
    private var onCalendarClickListener: OnCalendarClickListener? = null
    private var selectedDates = mutableListOf<CalendarDay>()

    fun setOnCalendarClickListener(listener: OnCalendarClickListener){
        this.onCalendarClickListener = listener
    }
    override fun initObserver() {

    }

    override fun initView() {
        binding.viewModel = viewModel
        val dayDecorator = DayDecorator(requireContext())
        val todayDecorator = TodayDecorator(requireContext())
        val sundayDecorator = SundayDecorator(requireContext())
        val saturdayDecorator = SaturdayDecorator(requireContext())
        var selectedMonthDecorator = SelectedMonthDecorator(requireContext(), CalendarDay.today().month)

        binding.calendarView.setTitleFormatter { day ->
            val inputText = day.date
            val calendarHeaderElements = inputText.toString().split("-")
            val calendarHeaderBuilder = StringBuilder()

            calendarHeaderBuilder.append(calendarHeaderElements[0]).append("년 ")
                .append(calendarHeaderElements[1]).append("월")

            calendarHeaderBuilder.toString()
        }

        binding.calendarView.addDecorators(dayDecorator, todayDecorator, sundayDecorator, saturdayDecorator, selectedMonthDecorator)
        binding.calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.custom_weekdays)))

        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            selectedDates = binding.calendarView.selectedDates
            selectedDates = selectedDates.sortedBy { it.date }.toMutableList()
            if (selectedDates.size >= 2){
                viewModel.setIsMultipleSelected(true)
                viewModel.setSelectedDate(selectedDates[0].date.toString())
                viewModel.setSelectedDate2(selectedDates[1].date.toString())
            } else{
                viewModel.setIsMultipleSelected(false)
            }
        }

        binding.calendarView.setOnMonthChangedListener { widget, date ->
            binding.calendarView.removeDecorators()
            binding.calendarView.invalidateDecorators()

            // Decorators 추가
            selectedMonthDecorator = SelectedMonthDecorator(requireContext(), date.month)
            binding.calendarView.addDecorators(dayDecorator, todayDecorator, sundayDecorator, saturdayDecorator, selectedMonthDecorator)
        }

        binding.doneBtn.setOnClickListener {
            dismissDialog()
        }

        binding.cancelBtn.setOnClickListener {
            dismissDialog()
        }
    }

    private fun dismissDialog(){
        selectedDates.clear()
        viewModel.setIsMultipleSelected(false)
        dismiss()
    }

    interface OnCalendarClickListener{
        fun onClick(date: CalendarDay)
    }
}