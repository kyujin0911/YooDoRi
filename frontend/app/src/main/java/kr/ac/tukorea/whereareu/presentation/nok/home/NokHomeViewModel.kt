package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
import kr.ac.tukorea.whereareu.domain.home.LastAddress
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceListInfo
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
        data class StartPredictEvent(val isPredicted: Boolean) : PredictEvent()
        data class MeaningFulPlaceEvent(
            val meaningfulPlaceForList: List<MeaningfulPlaceInfo>
        ) : PredictEvent()

        data class DementiaLastInfoEvent(val dementiaLastInfo: DementiaLastInfoResponse) :
            PredictEvent()

        data class LastLocationEvent(val lastAddress: LastAddress) : PredictEvent()

        data class SearchPoliceStationNearbyEvent(val policeStationList: List<PoliceStationInfo>) :
            PredictEvent()

        data class StopPredictEvent(val isPredicted: Boolean) : PredictEvent()
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
                eventPredict(PredictEvent.StartPredictEvent(true))
            } else {
                eventPredict(PredictEvent.StopPredictEvent(false))
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
                val meaningfulPlaceList = async { getMeaningfulPlace() }.await()


                val addressList = makeMeaningfulPlaceAddressList(meaningfulPlaceList)

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
                eventPredict(PredictEvent.DementiaLastInfoEvent(response))
                val address = viewModelScope.async {
                    getAddress(
                        response.lastLongitude.toString(),
                        response.lastLatitude.toString(),
                        true
                    )
                }.await()

                eventPredict(
                    PredictEvent.LastLocationEvent(
                        LastAddress(response.lastLatitude, response.lastLongitude, address)
                    )
                )
            }.onException {
                Log.d("error", it.toString())
            }
        return result
    }

    private suspend fun getAddress(x: String, y: String, isLastAddress: Boolean): String {
        var address = ""
        kakaoRepository.getAddress(x, y).onSuccess {
            address = convertResponseToAddress(it)
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

    private suspend fun getMeaningfulPlace(): List<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace> {
        var result = emptyList<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace>()
        nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
            Log.d("getMeaningfulPlace", response.toString())
            result = response.meaningfulLocations
            /*response.meaningfulLocations.forEach {
                val result = viewModelScope.async {
                    getAddress(it.longitude.toString(), it.latitude.toString(), false)
                }.await()
                addressList.add(result)
            }*/

            /*Log.d("meaningful address", addressList.toString())

            val meaningfulPlaces = response.meaningfulLocations.zip(addressList)
                .map {
                    MeaningfulPlace(
                        address = it.second,
                        date = it.first.dayOfTheWeek,
                        time = it.first.time,
                        latitude = it.first.latitude,
                        longitude = it.first.longitude
                    )
                }
            val meaningfulPlaceInfo = preprocessingList(meaningfulPlaces)
            eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceInfo))
            getPoliceStationInfoNearby(meaningfulPlaceInfo)
            getDementiaLastInfo()*/
        }.onException {
            Log.d("error", it.toString())
        }
        return result
    }

    private suspend fun makeMeaningfulPlaceAddressList(list: List<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace>): List<String> {
        val addressList = mutableListOf<String>()
        list.forEach {
            val result = viewModelScope.async {
                getAddress(it.longitude.toString(), it.latitude.toString(), false)
            }.await()
            addressList.add(result)
        }
        return addressList
    }

    private fun zipMeaningfulPlaceListWithAddress(
        meaningfulPlaceList: List<kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace>,
        addressList: List<String>
    ): List<MeaningfulPlace> {
        val result = meaningfulPlaceList.zip(addressList)
            .map {
                MeaningfulPlace(
                    address = it.second,
                    date = it.first.dayOfTheWeek,
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
                viewModelScope.async { searchPoliceStationNearby(meaningfulPlaceInfo) }.await()
            meaningfulPlaceInfo.policeStationInfo = result
        }
        Log.d("after police list", list.toString())
        eventPredict(PredictEvent.MeaningFulPlaceEvent(list))
        return list
    }

    private suspend fun searchPoliceStationNearby(meaningfulPlaceInfo: MeaningfulPlaceInfo): List<PoliceStationInfo> {
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
            eventPredict(PredictEvent.SearchPoliceStationNearbyEvent(policeList))
        }
        return result
    }

    private fun convertResponseToAddress(response: AddressResponse): String {
        val documents = response.documents[0]
        return if (documents.roadAddress == null)
            documents.address.addressName
        else {
            documents.roadAddress.addressName + " " + documents.roadAddress.buildingName
        }
    }

    private fun preprocessingList(list: List<MeaningfulPlace>): MutableList<MeaningfulPlaceInfo> {
        val groupList = list.groupBy { it.address }
        val meaningfulPlaceInfoList = mutableListOf<MeaningfulPlaceInfo>()
        groupList.keys.forEach { key ->
            val list = groupList[key]
            val meaningfulPlaceListInfo =
                list?.map { MeaningfulPlaceListInfo(date = it.date, time = it.time) }
                    ?.sortedBy { it.time }
            meaningfulPlaceInfoList.add(
                MeaningfulPlaceInfo(
                    key, meaningfulPlaceListInfo?.distinct()!!,
                    list.first().latitude, list.first().longitude
                )
            )
        }
        Log.d("tempList", meaningfulPlaceInfoList.toString())
        return meaningfulPlaceInfoList
    }
}