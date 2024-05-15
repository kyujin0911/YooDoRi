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
import kr.ac.tukorea.whereareu.data.model.nok.home.PredictLocationInfo
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.InnerItemClickEvent
import kr.ac.tukorea.whereareu.domain.home.LastLocation
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.DementiaStatusInfo
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

    private val _dementiaLocationInfo = MutableSharedFlow<LocationInfoResponse>()
    val dementiaLocationInfo = _dementiaLocationInfo.asSharedFlow()

    val dementiaStatusInfo = MutableStateFlow(DementiaStatusInfo())

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

    sealed class PredictEvent {
        data class StartPredict(val isPredicted: Boolean) : PredictEvent()
        data class MeaningFulPlace(
            val meaningfulPlaceForList: List<MeaningfulPlaceInfo>
        ) : PredictEvent()

        data class PredictLocation(
            val predictLocation: PredictLocationInfo
        ): PredictEvent()

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
                dementiaStatusInfo.value = DementiaStatusInfo(
                    it.userStatus, it.battery, it.isGpsOn, it.isInternetOn, it.isRingstoneOn
                )
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
                val dementiaLastInfo = async { getDementiaLastInfo() }.await()
                //Log.d("lastInfo", dementiaLastInfo.toString())
                val predictLocation = async { fetchPredictInfo() }.await()
                val meaningfulPlaceList = async { getMeaningfulPlaces() }.await()

            }
            Log.d("after refactor time", time.toString())
        }
    }

    private suspend fun getDementiaLastInfo() {
        nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest(_dementiaKey.value))
            .onSuccess { response ->
                Log.d("last info", response.toString())
                val averageSpeed = response.averageSpeed.div(3.6)
                val latLng = LatLng(response.lastLatitude, response.lastLongitude)

                eventPredict(PredictEvent.DisplayDementiaLastInfo(averageSpeed, latLng))

                eventPredict(
                    PredictEvent.DisplayDementiaLastLocation(
                        LastLocation(latLng, response.addressName)
                    )
                )
            }.onException {
                Log.d("error", it.toString())
            }
    }
    private suspend fun getMeaningfulPlaces() {
        nokHomeRepository.getMeaningfulPlace(_dementiaKey.value).onSuccess { response ->
            Log.d("getMeaningfulPlace", response.toString())
            val meaningfulPlaceInfo = response.meaningfulPlaces.map { meaningfulPlace ->
                val policeStationInfo = meaningfulPlace.policeStationInfo.map {policeStation ->
                    policeStation.toModel()
                }
                eventPredict(PredictEvent.SearchNearbyPoliceStation(policeStationInfo))
                meaningfulPlace.toModel(policeStationInfo)
            }

            eventPredict(PredictEvent.MeaningFulPlace(meaningfulPlaceInfo))
        }.onException {
            Log.d("error", it.toString())
        }
    }

    private suspend fun fetchPredictInfo(){
        nokHomeRepository.fetchPredictInfo(_dementiaKey.value).onSuccess { response ->
            eventPredict(PredictEvent.PredictLocation(response.predictLocation))
        }.onException {
            Log.d("predict exception", it.toString())
        }
    }
}