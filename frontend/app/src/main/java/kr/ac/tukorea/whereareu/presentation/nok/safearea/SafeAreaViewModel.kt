package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetCoordRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaDto
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.safearea.SafeAreaRepositoryImpl
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaRVA
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SafeAreaViewModel @Inject constructor(
    private val repository: SafeAreaRepositoryImpl,
    private val kakao: KakaoRepositoryImpl
) : ViewModel() {

    private val _dementiaKey = MutableStateFlow("")

    private val _safeAreaEvent = MutableSharedFlow<SafeAreaEvent>()
    val safeAreaEvent = _safeAreaEvent.asSharedFlow()

    val isSettingSafeArea = MutableStateFlow(false)

    private val _safeAreaRadius = MutableSharedFlow<Double>()
    val safeAreaRadius = _safeAreaRadius.asSharedFlow()
    sealed class SafeAreaEvent{
        data class FetchSafeArea(val safeAreas: List<SafeArea>, val groupNames: List<String>): SafeAreaEvent()

        data class FetchSafeAreaGroup(val safeAreas: List<SafeAreaDto>): SafeAreaEvent()

        data class MapView(val behavior: Int, val coord: LatLng) : SafeAreaEvent()

        data class RadiusChange(val radius: String): SafeAreaEvent()

        data class SettingSafeArea(val isSettingSafeArea: Boolean): SafeAreaEvent()

        data class CreateSafeAreaGroup(val groupName: String): SafeAreaEvent()
    }

    fun setIsSettingSafeAreaStatus(){
        isSettingSafeArea.value = isSettingSafeArea.value.not()
        eventSafeArea(SafeAreaEvent.SettingSafeArea(this.isSettingSafeArea.value))
    }

    fun setDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
    }

    fun setSafeAreaRadius(radius: String){
        eventSafeArea(SafeAreaEvent.RadiusChange(radius))
    }

    fun eventSafeArea(event: SafeAreaEvent){
        viewModelScope.launch{
            _safeAreaEvent.emit(event)
        }
    }

    fun registerSafeArea(request: RegisterSafeAreaRequest) {
        viewModelScope.launch {
            repository.registerSafeArea(request).onSuccess {
                Log.d("registerSafeArea", it.toString())
            }
        }
    }

    fun fetchSafeAreaAll() {
        viewModelScope.launch {
            repository.fetchSafeAreaAll(_dementiaKey.value).onSuccess {response ->
                val safeAreaList = mutableListOf<SafeArea>()
                val groupNameList = response.safeAreaList.map { it.groupName}.filterNot { it == "notGrouped" }

                response.safeAreaList.forEach { _safeAreaList ->
                    val temp = if (_safeAreaList.groupName == "notGrouped") {
                        _safeAreaList.safeAreas.map { safeArea ->
                            SafeArea(
                                "",
                                _safeAreaList.groupKey,
                                safeArea.areaKey,
                                safeArea.areaName,
                                safeArea.latitude,
                                safeArea.longitude,
                                safeArea.radius,
                                SafeAreaRVA.SAFE_AREA
                            )
                        }
                    } else {
                        _safeAreaList.safeAreas.map {
                            SafeArea(
                                _safeAreaList.groupName,
                                _safeAreaList.groupKey,
                                "",
                                "",
                                0.0,
                                0.0,
                                0,
                                SafeAreaRVA.SAFE_AREA_GROUP
                            )
                        }
                    }
                    safeAreaList.addAll(temp)
                }
                safeAreaList.sortWith(
                    compareBy(
                        {it.viewType},
                        {it.groupName},
                        {it.areaName}
                    )
                )
                eventSafeArea(SafeAreaEvent.FetchSafeArea(safeAreaList, groupNameList))
                Log.d("safeArea List", safeAreaList.toString())
                Log.d("fetchSafeArea", response.toString())
            }
        }
    }

    fun fetchSafeAreaGroup(groupKey: String){
        viewModelScope.launch {
            repository.fetchSafeAreaGroup(_dementiaKey.value, groupKey).onSuccess {
                eventSafeArea(SafeAreaEvent.FetchSafeAreaGroup(it.safeAreas))
                Log.d("fetchSafeAreaGroup", it.toString())
            }
        }
    }

    fun fetchCoord(address: String){
        viewModelScope.launch {
            repository.fetchCoord(GetCoordRequest(address)).onSuccess {
                Log.d("fetchCoord", it.toString())
            }
        }
    }

    /*fun fetchKeyword(){
        viewModelScope.launch {
            kakao.searchWithKeyword("안양시 동안구 비산로 22").onSuccess {
                Log.d("fetchKeyword", it.toString())
            }
        }
    }*/

    fun createSafeAreaGroup(groupName: String){
        eventSafeArea(SafeAreaEvent.CreateSafeAreaGroup(groupName))
    }
}