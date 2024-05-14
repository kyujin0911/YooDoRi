package kr.ac.tukorea.whereareu.presentation.nok.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.bp.LocalDate

class CalendarDialogViewModel: ViewModel() {
    private val _isMultipleSelected = MutableStateFlow(false)
    val isMultipleSelected = _isMultipleSelected.asStateFlow()

    val selectedDate = MutableStateFlow(LocalDate.MIN)
    val selectedDate2 = MutableStateFlow(LocalDate.MIN)

    fun setIsMultipleSelected(isMultipleSelected: Boolean){
        _isMultipleSelected.value = isMultipleSelected
    }

    fun setSelectedDate(date: LocalDate){
        selectedDate.value = date
    }

    fun setSelectedDate2(date: LocalDate){
        selectedDate2.value = date
    }
}