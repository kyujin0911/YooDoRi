package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.nok.home.DementiaLastInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class NokHomeViewModel @Inject constructor(
    val repository: NokHomeRepositoryImpl
) : ViewModel() {

    private val _dementiaLocation = MutableSharedFlow<LocationInfoResponse>(replay = 1)
    val dementiaLocation = _dementiaLocation.asSharedFlow()

    val isInternetOn = MutableStateFlow(true)
    val isGpsOn = MutableStateFlow(true)

    private val _updateDuration = MutableStateFlow<Long>(10 * 1000)
    val updateDuration = _updateDuration.asStateFlow()

    private val _isPredicted = MutableStateFlow(false)
    val isPredicted = _isPredicted.asStateFlow()

    private val _dementiaKey = MutableStateFlow("")

    private val _meaningfulPlace = MutableSharedFlow<MeaningfulPlaceResponse>()
    val meaningfulPlace = _meaningfulPlace.asSharedFlow()

    private val _predictEvent = MutableSharedFlow<PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()
    sealed class PredictEvent{
        data class MeaningFulPlaceEvent(val meaningfulPlace: MeaningfulPlaceResponse): PredictEvent()
        data class DementiaLastInfoEvent(val dementiaLastInfo: DementiaLastInfoResponse): PredictEvent()
    }

    private fun eventPredict(event: PredictEvent){
        viewModelScope.launch {
            _predictEvent.emit(event)
        }
    }

    fun saveDementiaKey(dementiaKey: String){
        _dementiaKey.value = dementiaKey
    }

    fun setIsPredicted(boolean: Boolean) {
        viewModelScope.launch {
            _isPredicted.emit(boolean)
        }
    }

    fun setUpdateDuration(duration: Long) {
        viewModelScope.launch {
            _updateDuration.emit(duration * 60 * 1000)
        }
    }

    fun getDementiaLocation() {
        viewModelScope.launch {
            repository.getDementiaLocationInfo(_dementiaKey.value).onSuccess {
                _dementiaLocation.emit(it)
                isInternetOn.value = it.isInternetOn
                isGpsOn.value = it.isGpsOn
            }.onError {
                Log.d("error", it.toString())
            }.onException {
                Log.d("exception", it.toString())
            }.onFail {
                Log.d("fail", it.toString())
            }
        }
    }

    private fun getMeaningfulPlace(){
        viewModelScope.launch {
            repository.getMeaningfulPlace("253050").onSuccess {
                Log.d("meaningful", it.toString())
                eventPredict(PredictEvent.MeaningFulPlaceEvent(it))
            }
        }
    }

    fun getDementiaLastInfo(){
        viewModelScope.launch {
            repository.getDementiaLastInfo(DementiaKeyRequest("253050")).onSuccess {
                Log.d("last info", it.toString())
                eventPredict(PredictEvent.DementiaLastInfoEvent(it))
                getMeaningfulPlace()
            }
        }
    }
}