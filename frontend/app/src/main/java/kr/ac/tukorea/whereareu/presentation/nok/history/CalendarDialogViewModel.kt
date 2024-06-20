package kr.ac.tukorea.whereareu.presentation.nok.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class CalendarDialogViewModel: ViewModel() {
    private val _isMultipleSelected = MutableStateFlow(false)
    val isMultipleSelected = _isMultipleSelected.asStateFlow()

    val selectedDate = MutableStateFlow(LocalDate.MIN)
    val selectedDate2 = MutableStateFlow(LocalDate.MIN)

    val selectedDates = MutableSharedFlow<List<LocalDate>>()

    fun setIsMultipleSelected(isMultipleSelected: Boolean){
        _isMultipleSelected.value = isMultipleSelected
    }

    fun setSelectedDates(dates: List<LocalDate>){
        viewModelScope.launch {
            selectedDates.emit(dates)
        }
    }


    fun setSelectedDate(date: LocalDate){
        selectedDate.value = date
    }

    fun setSelectedDate2(date: LocalDate){
        selectedDate2.value = date
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("dialog viewmodel", "mvlak")
    }
}