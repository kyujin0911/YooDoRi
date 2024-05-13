package kr.ac.tukorea.whereareu.presentation.nok.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.repository.nok.history.LocationHistoryRepositoryImpl
import kr.ac.tukorea.whereareu.domain.history.LocationHistory
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class LocationHistoryViewModel @Inject constructor(
    private val repository: LocationHistoryRepositoryImpl
) : ViewModel() {

    private val _locationHistory =
        MutableSharedFlow<List<kr.ac.tukorea.whereareu.domain.history.LocationHistory>>(

        )
    val locationHistory = _locationHistory.asSharedFlow()

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _maxProgress = MutableStateFlow(0)
    val maxProgress = _maxProgress.asStateFlow()

    private val _isLoadingComplete = MutableSharedFlow<Boolean>()
    val isLoadingComplete = _isLoadingComplete.asSharedFlow()

    fun setProgress(progress: Int) {
        _progress.value = progress
    }

    fun setIstLoading(isLoading: Boolean){
        viewModelScope.launch {
            _isLoadingComplete.emit(isLoading)
        }
    }

    fun fetchLocationHistory(date: String, dementiaKey: String) {
        viewModelScope.launch {
            repository.fetchLocationHistory(date, dementiaKey).onSuccess { response ->
                //val list = response.locationHistory.asSequence().withIndex().filter { it.index % 3 == 0 }.map { it.value }.toList()
                _maxProgress.value = response.locationHistory.indices.last
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
                    val list = tempList.map{
                        if (it.index == tempList.last().index){
                            LocationHistory(it.value.latitude,
                                it.value.longitude,
                                it.value.time,
                                it.value.userStatus,
                                it.value.distance,
                                true)
                        } else {
                            LocationHistory(it.value.latitude,
                                it.value.longitude,
                                it.value.time,
                                it.value.userStatus,
                                it.value.distance,
                                false)
                        }
                    }
                    Log.d("list", list.toString())
                    _locationHistory.emit(list)
                    _maxProgress.value = list.indices.last
                }
                Log.d("time", time.toString())
            }.onException {
                Log.d("errir", it.toString())
            }
        }
    }
}