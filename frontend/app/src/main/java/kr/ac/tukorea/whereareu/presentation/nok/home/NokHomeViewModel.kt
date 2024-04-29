package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.kakao.address.AddressResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.LastLocation
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class NokHomeViewModel @Inject constructor(
    private val nokHomeRepository: NokHomeRepositoryImpl,
    private val naverRepository: NaverRepositoryImpl,
    private val kakaoRepository: KakaoRepositoryImpl
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

    private val _predictEvent = MutableSharedFlow<PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

    sealed class PredictEvent {
        data class StartPredict(val isPredicted: Boolean) : PredictEvent()
        data class MeaningFulPlaceEvent(
            val meaningfulPlaceForList: List<MeaningfulPlaceInfo>
        ) : PredictEvent()

        data class DisplayDementiaLastInfo(val averageSpeed: Double, val coord: LatLng) :
            PredictEvent()

        data class DisplayDementiaLastLocation(val lastLocation: LastLocation) : PredictEvent()

        data class SearchNearbyPoliceStation(val policeStationList: List<PoliceStationInfo>) :
            PredictEvent()

        data class StopPredict(val isPredicted: Boolean) : PredictEvent()
    }

    private fun eventPredict(event: PredictEvent) {
        viewModelScope.launch {
            _predictEvent.emit(event)
        }
    }

    fun saveDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
    }

    fun setIsPredicted(isPredicted: Boolean) {
        viewModelScope.launch {
            _isPredicted.emit(isPredicted)
            if (isPredicted) {
                eventPredict(PredictEvent.StartPredict(true))
            } else {
                eventPredict(PredictEvent.StopPredict(false))
            }
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

    fun test() {
        viewModelScope.launch {
            val time = measureTimeMillis {
                val dementiaLastInfo = async { getDementiaLastInfo() }
                //Log.d("lastInfo", dementiaLastInfo.toString())
                val meaningfulPlaceList = async { getMeaningfulPlaces() }.await()

                //val zippedMeaningfulPlaceList = zipMeaningfulPlaceListWithAddress(meaningfulPlaceList, addressList)
                //val groupedMeaningfulPlaceList = preprocessingList(zippedMeaningfulPlaceList)

                //getPoliceStationInfoNearby(groupedMeaningfulPlaceList)
            }
            Log.d("after refactor time", time.toString())
        }
    }

    private suspend fun getDementiaLastInfo() {
        nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest("253050"))
            .onSuccess { response ->
                Log.d("last info", response.toString())
                val latitude = response.lastLatitude
                val longitude = response.lastLongitude
                val averageSpeed = response.averageSpeed.div(3.6)
                val coord = LatLng(latitude, longitude)

                eventPredict(PredictEvent.DisplayDementiaLastInfo(averageSpeed, coord))

                eventPredict(
                    PredictEvent.DisplayDementiaLastLocation(
                        LastLocation(latitude, longitude, response.addressName)
                    )
                )
            }.onException {
                Log.d("error", it.toString())
            }
    }
    private suspend fun getMeaningfulPlaces() {
        nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
            Log.d("getMeaningfulPlace", response.toString())
            val meaningfulPlaceInfo = response.meaningfulPlaces.map { meaningfulPlace ->
                eventPredict(PredictEvent.SearchNearbyPoliceStation(meaningfulPlace.policeStationInfo))

                MeaningfulPlaceInfo(
                    meaningfulPlace.address,
                    meaningfulPlace.timeInfo,
                    meaningfulPlace.latitude,
                    meaningfulPlace.longitude,
                    false,
                    meaningfulPlace.policeStationInfo
                )
            }

            eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceInfo))
        }.onException {
            Log.d("error", it.toString())
        }
    }
}