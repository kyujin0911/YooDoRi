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
): ViewModel() {

    private val _locationHistory = MutableStateFlow<LocationHistoryResponse>(
        LocationHistoryResponse(
    )
    )
    val locationHistory = _locationHistory.asStateFlow()
    fun fetchLocationHistory(date: String, dementiaKey: String){
        viewModelScope.launch {
            repository.fetchLocationHistory(date, dementiaKey).onSuccess { response ->
                _locationHistory.emit(response)
            }
        }
    }
}