package kr.ac.tukorea.whereareu.presentation.nok.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarDialogViewModel: ViewModel() {
    private val _isMultipleSelected = MutableStateFlow(false)
    val isMultipleSelected = _isMultipleSelected.asStateFlow()

    val selectedDate = MutableStateFlow("")
    val selectedDate2 = MutableStateFlow("")

    fun setIsMultipleSelected(isMultipleSelected: Boolean){
        _isMultipleSelected.value = isMultipleSelected
    }

    fun setSelectedDate(date: String){
        selectedDate.value = date
    }

    fun setSelectedDate2(date: String){
        selectedDate2.value = date
    }
}