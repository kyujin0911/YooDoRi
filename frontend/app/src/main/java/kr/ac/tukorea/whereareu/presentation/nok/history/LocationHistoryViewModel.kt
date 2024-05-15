package kr.ac.tukorea.whereareu.presentation.nok.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.repository.nok.history.LocationHistoryRepositoryImpl
import kr.ac.tukorea.whereareu.domain.history.LocationHistory
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class LocationHistoryViewModel @Inject constructor(
    private val repository: LocationHistoryRepositoryImpl
) : ViewModel() {

    private val _locationHistoryEvent = MutableSharedFlow<LocationHistoryEvent>()
    val locationHistoryEvent = _locationHistoryEvent.asSharedFlow()

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _progress2 = MutableStateFlow(0)
    val progress2 = _progress2.asStateFlow()

    private val _maxProgress = MutableStateFlow(0)
    val maxProgress = _maxProgress.asStateFlow()

    private val _maxProgress2 = MutableStateFlow(0)
    val maxProgress2 = _maxProgress2.asStateFlow()

    private val _isLoadingComplete = MutableSharedFlow<Boolean>()
    val isLoadingComplete = _isLoadingComplete.asSharedFlow()

    private val _dementiaKey = MutableStateFlow("")

    val isMultipleSelected = MutableStateFlow(false)

    sealed class LocationHistoryEvent {
        data class FetchSuccessSingle(val locationHistory: List<LocationHistory>) : LocationHistoryEvent()
        data class FetchSuccessMultiple(val locationHistory: List<List<LocationHistory>>) : LocationHistoryEvent()
        data object FetchFail : LocationHistoryEvent()

        data class OnProgressChanged(val progress: Int): LocationHistoryEvent()
        data class OnProgress2Changed(val progress: Int): LocationHistoryEvent()
    }

    private fun eventLocationHistory(event: LocationHistoryEvent) {
        viewModelScope.launch {
            _locationHistoryEvent.emit(event)
        }
    }
    fun setIsMultipleSelected(isMultipleSelected: Boolean){
        this.isMultipleSelected.value = isMultipleSelected
    }

    fun setProgress(progress: Int) {
        _progress.value = progress
    }

    fun setProgress2(progress: Int){
        _progress2.value = progress
    }

    fun setMaxProgress(maxProgress: Int) {
        _maxProgress.value = maxProgress
    }

    fun setDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
    }

    fun setIsLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _isLoadingComplete.emit(isLoading)
        }
    }

    fun fetchSingleLocationHistory(date: String) {
        viewModelScope.launch {
            val list = async { fetchLocationHistory(date) }.await()
            Log.d("fetchSingleLocationHistory list", list.toString())
            if (list.isNotEmpty()) {
                eventLocationHistory(LocationHistoryEvent.FetchSuccessSingle(list))
                _maxProgress.value = list.indices.last
            }
        }
    }

    fun fetchMultipleLocationHistory(date1: String, date2: String){
        viewModelScope.launch {
            val list = listOf( async { fetchLocationHistory(date1) },
            async { fetchLocationHistory(date2) }).awaitAll()
            if (list.isNotEmpty()){
                eventLocationHistory(LocationHistoryEvent.FetchSuccessMultiple(list))
                _maxProgress.value = list[0].indices.last
                _maxProgress2.value = list[1].indices.last
            }
        }
    }

    private suspend fun fetchLocationHistory(date: String): List<LocationHistory> {
        var result = emptyList<LocationHistory>()

        repository.fetchLocationHistory(date, _dementiaKey.value).onSuccess { response ->
            if (response.locationHistory.size < 2) {
                eventLocationHistory(LocationHistoryEvent.FetchFail)
                return emptyList()
            }
            //val list = response.locationHistory.asSequence().withIndex().filter { it.index % 3 == 0 }.map { it.value }.toList()
            //_maxProgress.value = response.locationHistory.indices.last
            val time = measureTimeMillis {
                /*val list = response.locationHistoryDto.mapIndexed { index, locationHistory ->
                    if (index != _maxProgress.value) {
                        kr.ac.tukorea.whereareu.domain.history.LocationHistory(
                            locationHistory.latitude,
                            locationHistory.longitude,
                            locationHistory.time,
                            locationHistory.userStatus,
                            locationHistory.distance,
                            false
                        )
                    } else {
                        kr.ac.tukorea.whereareu.domain.history.LocationHistory(
                            locationHistory.latitude,
                            locationHistory.longitude,
                            locationHistory.time,
                            locationHistory.userStatus,
                            locationHistory.distance,
                            true
                        )
                    }
                }*/
                val tempList = response.locationHistory.withIndex()
                val list = tempList.map {
                    val viewType = if (it.value.userStatus == "정지") {
                        LocationHistory.STOP_STATUS
                    } else {
                        LocationHistory.OTHER_STATUS
                    }

                    if (it.index == tempList.last().index) {
                        LocationHistory(
                            it.value.latitude,
                            it.value.longitude,
                            it.value.time,
                            it.value.userStatus,
                            it.value.distance,
                            true,
                            viewType,
                            date
                        )
                    } else {
                        LocationHistory(
                            it.value.latitude,
                            it.value.longitude,
                            it.value.time,
                            it.value.userStatus,
                            it.value.distance,
                            false,
                            viewType,
                            date
                        )
                    }
                }
                result = list
                //eventLocationHistory(LocationHistoryEvent.FetchSuccess(list))
                //_maxProgress.value = list.indices.last
            }
            Log.d("time", time.toString())
        }.onException {
            Log.d("errir", it.toString())
        }.onFail {
            eventLocationHistory(LocationHistoryEvent.FetchFail)
            Log.d("fail", it.toString())
        }
        return result
    }
}