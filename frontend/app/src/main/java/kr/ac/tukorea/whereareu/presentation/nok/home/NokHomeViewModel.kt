package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.kakao.Address
import kr.ac.tukorea.whereareu.data.model.kakao.AddressResponse
import kr.ac.tukorea.whereareu.data.model.kakao.Documents
import kr.ac.tukorea.whereareu.data.model.kakao.Meta
import kr.ac.tukorea.whereareu.data.model.kakao.RoadAddress
import kr.ac.tukorea.whereareu.data.model.nok.home.DementiaLastInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.LastAddress
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceListInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

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

    private val _meaningfulPlace = MutableSharedFlow<MeaningfulPlaceResponse>()
    val meaningfulPlace = _meaningfulPlace.asSharedFlow()

    private val _predictEvent = MutableSharedFlow<PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

    private val addressList = mutableListOf<String>()

    sealed class PredictEvent {
        data class MeaningFulPlaceEvent(val meaningfulPlace: List<MeaningfulPlaceInfo>) :
            PredictEvent()

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

    fun getDementiaLastInfo() {
        viewModelScope.launch {
            /*nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest("253050"))
                .onSuccess { response ->
                    Log.d("last info", response.toString())
                    eventPredict(PredictEvent.DementiaLastInfoEvent(response))
                    getAddress(
                        response.lastLongitude.toString(),
                        response.lastLatitude.toString(),
                        true
                    )
                }.onException {
                    Log.d("error", it.toString())
                }*/
            val response = DementiaLastInfoResponse(averageSpeed=0.23f, lastLatitude=37.401623, lastLongitude=126.9340687)
            eventPredict(PredictEvent.DementiaLastInfoEvent(response))
            getAddress(
                response.lastLongitude.toString(),
                response.lastLatitude.toString(),
                true
            )
        }
    }

    private fun getAddress(x: String, y: String, isLastAddress: Boolean) {
        viewModelScope.launch {
            /*kakaoRepository.getAddress(x, y).onSuccess {
                val address = convertResponseToAddress(it)
                if (isLastAddress) {
                    eventPredict(
                        PredictEvent.LastLocationEvent(
                            LastAddress(y.toDouble(), x.toDouble(), address)
                        )
                    )
                    getMeaningfulPlace()
                } else {
                    addressList.add(address)
                }
                Log.d("kakao api", it.toString())
            }.onError {
                Log.d("kakao api error", it.toString())
            }.onFail {
                Log.d("kakao api fail", it.toString())
            }.onException {
                Log.d("kakao api exception", it.toString())
            }*/
            val response = AddressResponse(meta= Meta(totalCount=1), documents= listOf(Documents(roadAddress= RoadAddress(addressName="경기도 안양시 동안구 비산로 22", buildingName="평촌자이아이파크"), address= Address(addressName="경기 안양시 동안구 비산동 1185"))))
            val address = convertResponseToAddress(response)
            if (isLastAddress) {
                eventPredict(
                    PredictEvent.LastLocationEvent(
                        LastAddress(y.toDouble(), x.toDouble(), address)
                    )
                )
                getMeaningfulPlace()
            } else {
                addressList.add(address)
            }
        }
    }

    private fun getMeaningfulPlace() {
        viewModelScope.launch {
            /*nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
                Log.d("getMeaningfulPlace", response.toString())
                response.meaningfulLocations.forEach {
                    getAddress(it.longitude.toString(), it.latitude.toString(), false)
                    delay(500)
                }

                Log.d("meaningful address", addressList.toString())

                val meaningfulPlaceList = response.meaningfulLocations.zip(addressList)
                    .map {
                        MeaningfulPlace(
                            address = it.second, date = it.first.dayOfTheWeek, time = it.first.time,
                            latitude = it.first.latitude, longitude = it.first.longitude
                        )
                    }
                val meaningfulPlaceInfo = preprocessingList(meaningfulPlaceList)
                eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceInfo))
            }.onException {
                Log.d("error", it.toString())
            }*/
            val meaningfulPlaceInfo = preprocessingList(emptyList())
            eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceInfo))
        }
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
        /*val groupList = list.groupBy { it.address }
        val meaningfulPlaceInfoList = mutableListOf<MeaningfulPlaceInfo>()
        groupList.keys.forEach { key ->
            val list = groupList[key]
            val meaningfulPlaceListInfo =
                list?.map { MeaningfulPlaceListInfo(date = it.date, time = it.time, index = 0, latitude = it.latitude, longitude = it.longitude) }
                    ?.sortedBy { it.time }
            meaningfulPlaceInfoList.add(MeaningfulPlaceInfo(key, meaningfulPlaceListInfo!!))
        }
        Log.d("tempList", meaningfulPlaceInfoList.toString())*/

        //api 없이 테스트
        val meaningfulPlaceInfoList = mutableListOf(MeaningfulPlaceInfo(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            meaningfulPlaceListInfo = listOf(
                MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0004",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0004",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0004",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0004",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0004",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0004",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0408",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0408",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "0408",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687
            ), MeaningfulPlaceListInfo(date = "Tuesday", time = "0812",
                    index = 0,
                    latitude = 37.401623,
                    longitude = 126.9340687))
        ), MeaningfulPlaceInfo(
            address = "경기 시흥시 정왕동 1308",
            meaningfulPlaceListInfo = listOf(MeaningfulPlaceListInfo(date = "Tuesday", time = "1620",
                index = 0,
                latitude = 37.401623,
                longitude = 126.9340687))
        ), MeaningfulPlaceInfo(
            address = "경기도 시흥시 산기대학로 237 한국공학대학교",
            meaningfulPlaceListInfo = listOf(MeaningfulPlaceListInfo(
                date = "Tuesday",
                time = "1620",
                index = 0,
                latitude = 37.401623,
                longitude = 126.9340687
            ), MeaningfulPlaceListInfo(date = "Tuesday", time = "1620",
                index = 0,
                latitude = 37.401623,
                longitude = 126.9340687))
        ))
        return meaningfulPlaceInfoList
    }
}