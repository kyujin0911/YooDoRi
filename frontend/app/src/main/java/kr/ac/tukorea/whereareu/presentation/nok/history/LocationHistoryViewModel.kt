package kr.ac.tukorea.whereareu.presentation.nok.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistory
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryRequest
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryResponse
import kr.ac.tukorea.whereareu.data.repository.nok.history.LocationHistoryRepository
import kr.ac.tukorea.whereareu.data.repository.nok.history.LocationHistoryRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class LocationHistoryViewModel @Inject constructor(
    private val repository: LocationHistoryRepositoryImpl
) : ViewModel() {

    private val _locationHistory = MutableStateFlow<List<LocationHistory>>(
        emptyList()
    )
    val locationHistory = _locationHistory.asStateFlow()

    private val _progress = MutableStateFlow(-1)
    val progress = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun setProgress(progress: Int) {
        if (!_isLoading.value) {
            _progress.value = progress
        }
    }

    fun setIsLoading(isLoading: Boolean){
        _isLoading.value = isLoading
    }

    fun fetchLocationHistory(date: String, dementiaKey: String) {
        viewModelScope.launch {
            repository.fetchLocationHistory(date, dementiaKey).onSuccess { response ->
                val list = response.locationHistory.asSequence().withIndex().filter { it.index % 3 == 0 }.map { it.value }
                _locationHistory.emit(list.toList())
            }
        }
    }
}