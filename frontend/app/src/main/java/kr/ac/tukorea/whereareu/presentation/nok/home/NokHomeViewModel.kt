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
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.InnerItemClickEvent
import kr.ac.tukorea.whereareu.domain.home.LastLocation
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

@HiltViewModel
class NokHomeViewModel @Inject constructor(
    private val nokHomeRepository: NokHomeRepositoryImpl,
    private val naverRepository: NaverRepositoryImpl,
    private val kakaoRepository: KakaoRepositoryImpl
) : ViewModel() {

    private val _dementiaLocationInfo = MutableStateFlow<LocationInfoResponse>(LocationInfoResponse())
    val dementiaLocationInfo = _dementiaLocationInfo.asStateFlow()

    private val _updateRate = MutableStateFlow<Long>(0L)
    val updateRate = _updateRate.asStateFlow()

    private val _isPredicted = MutableStateFlow(false)
    val isPredicted = _isPredicted.asStateFlow()

    private val _dementiaKey = MutableStateFlow("")
    private val _nokKey = MutableStateFlow("")

    private val _predictEvent = MutableSharedFlow<PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

    private val _dementiaName = MutableStateFlow("")
    val dementiaName = _dementiaName.asStateFlow()

    private val _navigateEvent = MutableStateFlow(NavigateEvent.Home.toString())
    val navigateEvent = _navigateEvent.asStateFlow()

    private val _innerItemClickEvent = MutableSharedFlow<InnerItemClickEvent>()
    val innerItemClickEvent = _innerItemClickEvent.asSharedFlow()

    private val _currentSpeed = MutableStateFlow("")
    val currentSpeed = _currentSpeed.asStateFlow()

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

    sealed interface NavigateEvent{
        data object Home: NavigateEvent
        data object Setting: NavigateEvent
        data object MeaningfulPlace: NavigateEvent
        data object LocationHistory: NavigateEvent
        data object SafeArea: NavigateEvent
    }

    fun eventNavigate(event: NavigateEvent){
        viewModelScope.launch {
            _navigateEvent.value = event.toString()
        }
    }

    private fun eventPredict(event: PredictEvent) {
        viewModelScope.launch {
            _predictEvent.emit(event)
        }
    }

    fun eventInnerItemClick(event: InnerItemClickEvent){
        viewModelScope.launch {
            _innerItemClickEvent.emit(event)
        }
    }

    fun setDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
    }

    fun setNokKey(nokKey: String) {
        _nokKey.value = nokKey
    }

    fun setUpdateRate(updateRate: Long){
        _updateRate.value = updateRate
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

    fun fetchUserInfo(){
        viewModelScope.launch {
            nokHomeRepository.getUserInfo(_nokKey.value).onSuccess {
                _dementiaName.emit(it.dementiaInfoRecord.dementiaName)
                _updateRate.value = it.nokInfoRecord.updateRate.toLong()
                Log.d("fetchUserInfo", _updateRate.value.toString())
            }
        }
    }

    fun getDementiaLocation() {
        viewModelScope.launch {
            nokHomeRepository.getDementiaLocationInfo(_dementiaKey.value).onSuccess {
                _dementiaLocationInfo.emit(it)
                _currentSpeed.value = (it.currentSpeed*3.6).roundToInt().toString()
            }.onError {
                Log.d("error", it.toString())
            }.onException {
                Log.d("exception", it.toString())
            }.onFail {
                Log.d("fail", it.toString())
            }
        }
    }

    fun predict() {
        viewModelScope.launch {
            val time = measureTimeMillis {
                val dementiaLastInfo = async { getDementiaLastInfo() }
                //Log.d("lastInfo", dementiaLastInfo.toString())
                val meaningfulPlaceList = async { getMeaningfulPlaces() }.await()
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