package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.utils.MapUtils.isNullOrEmpty
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
import kr.ac.tukorea.whereareu.data.model.kakao.AddressResponse
import kr.ac.tukorea.whereareu.data.model.naver.ReverseGeocodingResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.DementiaLastInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepository
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.LastAddress
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.Temp
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

    fun getDementiaLastInfo() {
        viewModelScope.launch {
            nokHomeRepository.getDementiaLastInfo(DementiaKeyRequest("253050"))
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
                }
        }
    }

    private fun getAddress(x: String, y: String, isLastAddress: Boolean) {
        viewModelScope.launch {
            kakaoRepository.getAddress(x, y).onSuccess {
                val address = convertResponseToAddress(it)
                if (isLastAddress) {
                    eventPredict(
                        PredictEvent.LastLocationEvent(
                            LastAddress(
                                y.toDouble(), x.toDouble(),
                                address
                            )
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
            }
        }
    }

    private fun getMeaningfulPlace() {
        viewModelScope.launch {
            nokHomeRepository.getMeaningfulPlace("253050").onSuccess { response ->
                Log.d("getMeaningfulPlace", response.toString())
                response.meaningfulLocations.forEach {
                    getAddress(it.longitude.toString(), it.latitude.toString(), false)
                    delay(500)
                }

                Log.d("meaningful address", addressList.toString())

                /*val dateList = response.meaningfulLocations.map { it.date }
                val timeList = response.meaningfulLocations.map { it.time }
                val meaningfulPlaceList = mutableListOf<MeaningfulPlace>()
                for (i in response.meaningfulLocations.indices) {
                    meaningfulPlaceList.add(
                        MeaningfulPlace(
                            date = dateList[i],
                            time = timeList[i],
                            address = addressList[i]
                        )
                    )
                }

                Log.d("meaningful place", meaningfulPlaceList.toString())

                eventPredict(PredictEvent.MeaningFulPlaceEvent(meaningfulPlaceList))*/
            }.onException {
                Log.d("error", it.toString())
            }
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

    fun makeList(){
        val list = listOf(MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0408"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0004"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0408"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0004"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0004"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0004"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0004"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0408"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0004"
        ), MeaningfulPlace(
            address = "서울특별시 용산구 이촌로2가길36 중산아파트 1 동",
            date = "Tuesday",
            time = "0812"
        ), MeaningfulPlace(
            address = "경기 시흥시 정왕동1308",
            date = "Tuesday",
            time = "1620"
        ), MeaningfulPlace(
            address = "경기도 시흥시 산기대학로237 한국공학대학교",
            date = "Tuesday",
            time = "1620"
        ), MeaningfulPlace(
            address = "경기도 시흥시 산기대학로237 한국공학대학교",
            date = "Tuesday",
            time = "1620"
        ))
        val groupList = list.groupBy { it.address }
        val tempList = mutableListOf<Temp>()
        groupList.keys.forEach {key ->
            val list = groupList[key]
            val meaningfulPlaceInfoList = list?.map { MeaningfulPlaceInfo(date = it.date, time = it.time) }?.sortedBy { it.time }
            tempList.add(Temp(key, meaningfulPlaceInfoList!!))
        }
        Log.d("tempList", tempList.toString())
    }
}