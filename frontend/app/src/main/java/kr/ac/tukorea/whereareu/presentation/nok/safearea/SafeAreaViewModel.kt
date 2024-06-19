package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetCoordRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaGroupRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaDto
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaGroup
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.safearea.SafeAreaRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SafeAreaViewModel@Inject constructor(
    private val repository: SafeAreaRepositoryImpl,
) : ViewModel() {

    private val _dementiaKey = MutableStateFlow("")

    private val _safeAreaEvent = MutableSharedFlow<SafeAreaEvent>()
    val safeAreaEvent = _safeAreaEvent.asSharedFlow()

    val isSettingSafeArea = MutableStateFlow(false)

    private val _safeAreaGroupList = MutableStateFlow<List<SafeAreaGroup>>(emptyList())

    private val _isSafeAreaGroupChanged = MutableStateFlow(true)
    val isSafeAreaGroupChanged = _isSafeAreaGroupChanged.asStateFlow()

    private val _selectedSafeAreaGroup = MutableStateFlow<SafeAreaGroup>(SafeAreaGroup("", ""))
    val selectedSafeAreaGroup = _selectedSafeAreaGroup.asStateFlow()

    val settingSafeAreaName = MutableStateFlow("")

    private val _settingSafeAreaRadius = MutableStateFlow(0.5f)
    val settingSafeAreaRadius = _settingSafeAreaRadius.asStateFlow()

    private val _settingSafeAreaLatLng = MutableStateFlow(LatLng(0.0,0.0))
    val settingSafeAreaLatLng = _settingSafeAreaLatLng.asStateFlow()

    private val _currentGroup = MutableStateFlow("기본 그룹")
    val currentGroup = _currentGroup.asStateFlow()


    sealed class SafeAreaEvent{
        data class FetchSafeArea(val groupList: List<SafeAreaGroup>): SafeAreaEvent()

        data class FetchSafeAreaGroup(val safeAreas: List<SafeAreaDto>): SafeAreaEvent()

        data class MapView(val behavior: Int, val coord: LatLng) : SafeAreaEvent()

        data class RadiusChange(val radius: String): SafeAreaEvent()

        data class ChangeSafeAreaGroup(val groupName: String): SafeAreaEvent()

        data class SettingSafeArea(val isSettingSafeArea: Boolean): SafeAreaEvent()

        data class CreateSafeAreaGroup(val groupName: String): SafeAreaEvent()
        data object SuccessRegisterSafeArea: SafeAreaEvent()

        data object ExitDetailFragment: SafeAreaEvent()
        data class FailRegisterSafeArea(val message: String): SafeAreaEvent()
        data class FetchCoord(val coord: LatLng): SafeAreaEvent()
    }

    fun setIsSafeAreaGroupChanged(isSafeAreaGroupChanged: Boolean){
        _isSafeAreaGroupChanged.value = isSafeAreaGroupChanged
    }

    fun setIsSettingSafeAreaStatus(isSettingSafeArea: Boolean){
        this.isSettingSafeArea.value = isSettingSafeArea
        eventSafeArea(SafeAreaEvent.SettingSafeArea(this.isSettingSafeArea.value))
    }

    fun setDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
    }

    fun setSafeAreaRadius(radius: String){
        eventSafeArea(SafeAreaEvent.RadiusChange(radius))
        _settingSafeAreaRadius.value = radius.toFloat()
    }

    fun setSettingSafeAreaLatLng(coord: LatLng){
        _settingSafeAreaLatLng.value = coord
    }

    fun setCurrentGroup(groupKey: String){
        //getSafeAreaName(groupKey)
        _currentGroup.value = getSafeAreaName(groupKey)
    }

    fun eventSafeArea(event: SafeAreaEvent){
        viewModelScope.launch{
            _safeAreaEvent.emit(event)
        }
    }

    fun registerSafeArea() {
        viewModelScope.launch {
            if (settingSafeAreaName.value.isEmpty()){
                eventSafeArea(SafeAreaEvent.FailRegisterSafeArea("안심구역 이름을 입력해주세요."))
                return@launch
            }
            repository.registerSafeArea(RegisterSafeAreaRequest(
                _dementiaKey.value,
                _selectedSafeAreaGroup.value.groupKey,
                settingSafeAreaName.value,
                _settingSafeAreaLatLng.value.latitude,
                _settingSafeAreaLatLng.value.longitude,
                _settingSafeAreaRadius.value
            )).onSuccess {
                Log.d("registerSafeArea", it.toString())
                eventSafeArea(SafeAreaEvent.SuccessRegisterSafeArea)
                eventSafeArea(SafeAreaEvent.SettingSafeArea(false))
                isSettingSafeArea.value = false
            }
        }
    }

    fun registerSafeAreaGroup(groupName: String){
        viewModelScope.launch {
            repository.registerSafeAreaGroup(RegisterSafeAreaGroupRequest(_dementiaKey.value, groupName)).onSuccess {
                _isSafeAreaGroupChanged.value = true
                eventSafeArea(SafeAreaEvent.CreateSafeAreaGroup(groupName))
                Log.d("registerSafeAreaGroup", it.toString())
            }.onException {
                Log.d("registerSafeAreaGroup", it.toString())
            }
        }
    }

    fun fetchSafeAreaAll() {
        if(!_isSafeAreaGroupChanged.value){
            return
        }
        viewModelScope.launch {
            repository.fetchSafeAreaAll(_dementiaKey.value).onSuccess {response ->
                val groupList = response.groupList.sortedBy { it.groupName }
                //val safeAreaList = mutableListOf<SafeArea>()
                //val groupNameList = response.safeAreaList.map { it.groupName}.filterNot { it == "notGrouped" }

                /*response.safeAreaList.forEach { _safeAreaList ->
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
                )*/
                //savedStateHandle["safeAreaGroupList"] = groupList
                _safeAreaGroupList.value = groupList
                eventSafeArea(SafeAreaEvent.FetchSafeArea(groupList))
                //Log.d("safeArea List", safeAreaList.toString())
                Log.d("fetchSafeArea", response.toString())
                _isSafeAreaGroupChanged.value = false
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
                val latLng = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                eventSafeArea(SafeAreaEvent.FetchCoord(latLng))
                Log.d("fetchCoord", it.toString())
            }
        }
    }

    fun createSafeAreaGroup(groupName: String){
        eventSafeArea(SafeAreaEvent.CreateSafeAreaGroup(groupName))
    }

    fun getSafeAreaGroupList(): List<SafeAreaGroup>{
        return _safeAreaGroupList.value
    }

    fun getSafeAreaName(groupKey: String): String{
        val groupName = _safeAreaGroupList.value.find { it.groupKey == groupKey }?.groupName
        Log.d("groupName", groupName.toString())
        return groupName ?: ""
    }

    fun setSelectedSafeAreaGroup(groupName: String){
        _selectedSafeAreaGroup.value = _safeAreaGroupList.value.find { it.groupName == groupName }!!
        eventSafeArea(SafeAreaEvent.ChangeSafeAreaGroup(groupName))
    }
}