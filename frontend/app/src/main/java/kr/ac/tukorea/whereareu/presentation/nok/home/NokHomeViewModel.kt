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

    private val _updateRate = MutableStateFlow<Long>(300000 * 1000)
    val updateRate = _updateRate.asStateFlow()

    private val _isPredicted = MutableStateFlow(false)
    val isPredicted = _isPredicted.asStateFlow()

    private val _dementiaKey = MutableStateFlow("")
    private val _nokKey = MutableStateFlow("")

    private val _predictEvent = MutableSharedFlow<PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

    private val _dementiaName = MutableStateFlow("")
    val dementiaName = _dementiaName.asStateFlow()

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
    private val userMeaningfulPlace = mutableListOf<MeaningfulPlaceInfo>()

    private fun eventPredict(event: PredictEvent) {
        viewModelScope.launch {
            _predictEvent.emit(event)
        }
    }

    fun setDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
    }

    fun setNokKey(nokKey: String) {
        _nokKey.value = nokKey
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
            _updateRate.emit(duration * 60 * 1000)
        }
    }

    fun fetchUserInfo(){
        viewModelScope.launch {
            nokHomeRepository.getUserInfo(_nokKey.value).onSuccess {
                _dementiaName.emit(it.dementiaInfoRecord.dementiaName)
            }
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

//    private fun getDementiaLastInfo() {
//        viewModelScope.launch {
//            nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest("253050"))
//                .onSuccess { response ->
//                    Log.d("last info", response.toString())
//                    eventPredict(PredictEvent.DementiaLastInfoEvent(response))
//                    getAddress(
//                        response.lastLongitude.toString(),
//                        response.lastLatitude.toString(),
//                        true
//                    )
//                }.onException {
//                    Log.d("error", it.toString())
//                }
//        }
//    }
//
//    private fun getAddress(x: String, y: String, isLastAddress: Boolean) {
//        viewModelScope.launch {
//            kakaoRepository.getAddress(x, y).onSuccess {
//                val address = convertResponseToAddress(it)
//                if (isLastAddress) {
//                    eventPredict(
//                        PredictEvent.LastLocationEvent(
//                            LastAddress(y.toDouble(), x.toDouble(), address)
//                        )
//                    )
//                } else {
//                    addressList.add(address)
//                }
//                Log.d("kakao api", it.toString())
//            }.onError {
//                Log.d("kakao api error", it.toString())
//            }.onFail {
//                Log.d("kakao api fail", it.toString())
//            }.onException {
//                Log.d("kakao api exception", it.toString())
//            }
//        }
//    }
//
//    fun getMeaningfulPlace() {
//        viewModelScope.launch {
//            nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
//                Log.d("getMeaningfulPlace", response.toString())
//                response.meaningfulLocations.forEach {
//                    getAddress(it.longitude.toString(), it.latitude.toString(), false)
//                    delay(500)
//                }
//
//                Log.d("meaningful address", addressList.toString())
//
//                val meaningfulPlaces = response.meaningfulLocations.zip(addressList)
//                    .map {
//                        MeaningfulPlace(
//                            address = it.second, date = it.first.dayOfTheWeek, time = it.first.time,
//                            latitude = it.first.latitude, longitude = it.first.longitude
//                        )
//                    }
//                val meaningfulPlaceInfo = preprocessingList(meaningfulPlaces)
//                eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceInfo))
//                getPoliceStationInfoNearby(meaningfulPlaceInfo)
//                getDementiaLastInfo()
//            }.onException {
//                Log.d("error", it.toString())
//            }
//        }
//    }

//    private fun getPoliceStationInfoNearby(list: MutableList<MeaningfulPlaceInfo>){
//        viewModelScope.launch {
//            list.forEach { meaningfulPlaceInfo ->
//                searchPoliceStationNearby(meaningfulPlaceInfo)
//                delay(300)
//            }
//            Log.d("after police list", list.toString())
//        }
//    }
//
//    private fun searchPoliceStationNearby(meaningfulPlaceInfo: MeaningfulPlaceInfo){
//        viewModelScope.launch {
//            val x = meaningfulPlaceInfo.longitude.toString()
//            val y = meaningfulPlaceInfo.latitude.toString()
//            kakaoRepository.searchWithKeyword(x, y).onSuccess {
//                Log.d("kakao keyword", it.toString())
//                val policeList = it.documents.filter {document ->
//                    document.roadAddressName.isNullOrEmpty().not() or document.phone.isNullOrEmpty().not() }
//                    .map { document ->
//                    PoliceStationInfo(document.placeName, document.distance, document.roadAddressName, document.phone,
//                        document.x, document.y)
//                }.take(3)
//                meaningfulPlaceInfo.policeStationInfo = policeList
//                eventPredict(PredictEvent.SearchPoliceStationNearbyEvent(policeList))
//                Log.d("police list", policeStationInfoList.toString())
//            }
//        }
//    }
//
//    private fun convertResponseToAddress(response: AddressResponse): String {
//        val documents = response.documents[0]
//        return if (documents.roadAddress == null)
//            documents.address.addressName
//        else {
//            documents.roadAddress.addressName + " " + documents.roadAddress.buildingName
//        }
//    }
//
//    private fun preprocessingList(list: List<MeaningfulPlace>): MutableList<MeaningfulPlaceInfo> {
//        val groupList = list.groupBy { it.address }
//        val meaningfulPlaceInfoList = mutableListOf<MeaningfulPlaceInfo>()
//        groupList.keys.forEach { key ->
//            val list = groupList[key]
//            val meaningfulPlaceListInfo =
//                list?.map { MeaningfulPlaceListInfo(date = it.date, time = it.time) }
//                    ?.sortedBy { it.time }
//            meaningfulPlaceInfoList.add(MeaningfulPlaceInfo(key, meaningfulPlaceListInfo?.distinct()!!,
//                list.first().latitude, list.first().longitude))
//        }
//        Log.d("tempList", meaningfulPlaceInfoList.toString())
//        return meaningfulPlaceInfoList
//    }
}