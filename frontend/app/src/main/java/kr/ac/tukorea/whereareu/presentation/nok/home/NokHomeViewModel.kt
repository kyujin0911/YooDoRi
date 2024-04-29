package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.kakao.address.AddressResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.DementiaLastInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.LastLocation
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.TimeInfo
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

        data class DisplayDementiaLastInfo(val dementiaLastInfo: DementiaLastInfoResponse) :
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
                Log.d("lastInfo", dementiaLastInfo.toString())
                val meaningfulPlaceList = async { getMeaningfulPlaces() }.await()

                val addressList = getAddressList(meaningfulPlaceList)

                val zippedMeaningfulPlaceList =
                    zipMeaningfulPlaceListWithAddress(meaningfulPlaceList, addressList)
                val groupedMeaningfulPlaceList = preprocessingList(zippedMeaningfulPlaceList)

                getPoliceStationInfoNearby(groupedMeaningfulPlaceList)
            }
            Log.d("after refactor time", time.toString())
        }
    }

    private suspend fun getDementiaLastInfo(): DementiaLastInfoResponse {
        var result = DementiaLastInfoResponse()

        nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest("253050"))
            .onSuccess { response ->
                result = response
                Log.d("last info", response.toString())
                eventPredict(PredictEvent.DisplayDementiaLastInfo(response))
                val address = viewModelScope.async {
                    getAddress(
                        response.lastLongitude.toString(),
                        response.lastLatitude.toString(),
                    )
                }.await()

                eventPredict(
                    PredictEvent.DisplayDementiaLastLocation(
                        LastLocation(response.lastLatitude, response.lastLongitude, address)
                    )
                )
            }.onException {
                Log.d("error", it.toString())
            }
        return result
    }

    private suspend fun getAddress(x: String, y: String): String {
        var address = ""

        kakaoRepository.getAddress(x, y).onSuccess {
            address = convertDocumentToAddress(it)
            Log.d("kakao api", it.toString())
        }.onError {
            Log.d("kakao api error", it.toString())
        }.onFail {
            Log.d("kakao api fail", it.toString())
        }.onException {
            Log.d("kakao api exception", it.toString())
        }
        return address
    }

    private suspend fun getMeaningfulPlaces(): List<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace> {
        var result = emptyList<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace>()

        nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
            Log.d("getMeaningfulPlace", response.toString())
            result = response.meaningfulLocations
        }.onException {
            Log.d("error", it.toString())
        }
        return result
    }

    private suspend fun getAddressList(list: List<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace>): List<String> {
        val addressList = mutableListOf<String>()

        list.forEach {
            val result = viewModelScope.async {
                getAddress(it.longitude.toString(), it.latitude.toString())
            }.await()
            addressList.add(result)
        }
        return addressList
    }

    // Geocoding으로 얻은 주소와 의미장소 매핑
    private fun zipMeaningfulPlaceListWithAddress(
        meaningfulPlaceList: List<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace>,
        addressList: List<String>
    ): List<MeaningfulPlace> {
        val result = meaningfulPlaceList.zip(addressList)
            .map {
                MeaningfulPlace(
                    address = it.second,
                    dayOfWeek = it.first.dayOfTheWeek,
                    time = it.first.time,
                    latitude = it.first.latitude,
                    longitude = it.first.longitude
                )
            }
        return result
    }

    private suspend fun getPoliceStationInfoNearby(list: MutableList<MeaningfulPlaceInfo>): List<MeaningfulPlaceInfo> {
        list.forEach { meaningfulPlaceInfo ->
            val result =
                viewModelScope.async { searchNearbyPoliceStation(meaningfulPlaceInfo) }.await()
            meaningfulPlaceInfo.policeStationInfo = result
        }

        Log.d("after police list", list.toString())
        eventPredict(PredictEvent.MeaningFulPlaceEvent(list))
        return list
    }

    private suspend fun searchNearbyPoliceStation(meaningfulPlaceInfo: MeaningfulPlaceInfo): List<PoliceStationInfo> {
        var result = emptyList<PoliceStationInfo>()

        val x = meaningfulPlaceInfo.longitude.toString()
        val y = meaningfulPlaceInfo.latitude.toString()

        kakaoRepository.searchWithKeyword(x, y).onSuccess {
            Log.d("kakao keyword", it.toString())
            val policeList = it.documents.filter { document ->
                document.roadAddressName.isNotEmpty() or document.phone.isNotEmpty()
            }.map { document ->
                PoliceStationInfo(
                    document.placeName,
                    document.distance,
                    document.roadAddressName,
                    document.phone,
                    document.x,
                    document.y
                )
            }.take(3)

            result = policeList
            eventPredict(PredictEvent.SearchNearbyPoliceStation(policeList))
        }
        return result
    }

    // Geocoding response로 받은 document 주소로 변환
    private fun convertDocumentToAddress(response: AddressResponse): String {
        val document = response.documents[0]
        return if(document.roadAddress.addressName == null){
            document.address.addressName
        } else {
            document.roadAddress.addressName + " " + document.roadAddress.buildingName
        }
    }

    // MeaningfulPlaceRVA에 데이터를 넣기 위한 전처리 작업
    private fun preprocessingList(list: List<MeaningfulPlace>): MutableList<MeaningfulPlaceInfo> {

        // 주소를 기준으로 의미장소 리스트 그룹화
        val groupList = list.groupBy { it.address }
        val meaningfulPlaceInfoList = mutableListOf<MeaningfulPlaceInfo>()

        // 동일한 주소에 대한 시간 정보 리스트 생성
        groupList.keys.forEach { address ->
            val list = groupList[address]
            val timeInfoList =
                list?.map { TimeInfo(it.dayOfWeek, it.time) }
                    ?.sortedBy { it.time }

            // 의미장소 리스트에 방문한 시간정보 추가
            meaningfulPlaceInfoList.add(
                MeaningfulPlaceInfo(
                    address, timeInfoList?.distinct()!!,
                    list.first().latitude, list.first().longitude
                )
            )
        }
        Log.d("tempList", meaningfulPlaceInfoList.toString())
        return meaningfulPlaceInfoList
    }
}