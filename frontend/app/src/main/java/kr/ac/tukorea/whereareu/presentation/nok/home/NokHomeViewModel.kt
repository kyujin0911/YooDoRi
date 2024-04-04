package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.naver.ReverseGeocodingResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.DementiaLastInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.LastAddress
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class NokHomeViewModel @Inject constructor(
    private val nokHomeRepository: NokHomeRepositoryImpl,
    private val naverRepository: NaverRepositoryImpl
) : ViewModel() {

    private val _dementiaLocation = MutableSharedFlow<LocationInfoResponse>(replay = 1)
    val dementiaLocation = _dementiaLocation.asSharedFlow()

    val isInternetOn = MutableStateFlow(true)
    val isGpsOn = MutableStateFlow(true)

    private val _updateDuration = MutableStateFlow<Long>(300000 * 1000)
    val updateDuration = _updateDuration.asStateFlow()

    private val _isPredicted = MutableStateFlow(false)
    val isPredicted = _isPredicted.asStateFlow()

    private val _dementiaKey = MutableStateFlow("")

    private val _meaningfulPlace = MutableSharedFlow<MeaningfulPlaceResponse>()
    val meaningfulPlace = _meaningfulPlace.asSharedFlow()

    private val _predictEvent = MutableSharedFlow<PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

    private val addressList = mutableListOf<String>()

    sealed class PredictEvent {
        data class MeaningFulPlaceEvent(val meaningfulPlace: List<MeaningfulPlace>) : PredictEvent()
        data class DementiaLastInfoEvent(val dementiaLastInfo: DementiaLastInfoResponse) :
            PredictEvent()

        data class LastLocationEvent(val lastAddress: LastAddress) : PredictEvent()
    }

    private fun eventPredict(event: PredictEvent) {
        viewModelScope.launch {
            _predictEvent.emit(event)
        }
    }

    fun saveDementiaKey(dementiaKey: String) {
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
            nokHomeRepository.getDementiaLocationInfo(_dementiaKey.value).onSuccess {
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

    private fun getMeaningfulPlace() {
        viewModelScope.launch {
            nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
                Log.d("meaningful response", response.toString())
                response.meaningfulLocations.forEach { meaningfulPlace ->
                    getReverseGeocoding(
                            "${meaningfulPlace.longitude},${meaningfulPlace.latitude}",
                            meaningfulPlace.latitude, meaningfulPlace.longitude
                        )
                    delay(500)
                }
                Log.d("meaningful address", addressList.toString())

                val dateList = response.meaningfulLocations.map { it.date }
                val timeList = response.meaningfulLocations.map { it.time }
                val meaningfulPlaceList = mutableListOf<MeaningfulPlace>()
                for (i in response.meaningfulLocations.indices){
                    meaningfulPlaceList.add(MeaningfulPlace(dateList[i], timeList[i], addressList[i]))
                }

                Log.d("meaningful place", meaningfulPlaceList.toString())

                eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceList))
            }.onException {
                Log.d("error", it.toString())
            }
        }
    }

    fun getDementiaLastInfo() {
        viewModelScope.launch {
            nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest("253050"))
                .onSuccess { response ->
                    Log.d("last info", response.toString())
                    eventPredict(PredictEvent.DementiaLastInfoEvent(response))
                    getLastLocationAddress(
                        "${response.lastLongitude},${response.lastLatitude}",
                        response.lastLatitude,
                        response.lastLongitude
                    )
                }.onException {
                    Log.d("error", it.toString())
                }
        }
    }

    private fun getReverseGeocoding(coords: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            naverRepository.getReverseGeocodingInfo(coords, "addr", "json")
                .onSuccess { response ->
                    Log.d("reverse Geocoding", response.toString())
                    addressList.add(setAddress(response))
                }.onException {
                    Log.d("error", it.toString())
                }
        }
    }

    private fun getLastLocationAddress(coords: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            naverRepository.getReverseGeocodingInfo(coords, "addr", "json")
                .onSuccess { response ->
                    Log.d("reverse Geocoding", response.toString())
                    val address = setAddress(response)
                    Log.d("address result", address)
                    eventPredict(
                        PredictEvent.LastLocationEvent(
                            LastAddress(
                                latitude,
                                longitude,
                                address
                            )
                        )
                    )
                    getMeaningfulPlace()
                }.onException {
                    Log.d("error", it.toString())
                }
        }
    }

    private fun setAddress(response: ReverseGeocodingResponse): String {
        val region = response.results[0].region
        val land = response.results[0].land
        return "${region.area1.name} ${region.area2.name} " +
                "${region.area3.name} ${land.number1}" +
                if (land.number2.isNullOrEmpty()) {
                    ""
                } else "-${land.number2}"
    }
}