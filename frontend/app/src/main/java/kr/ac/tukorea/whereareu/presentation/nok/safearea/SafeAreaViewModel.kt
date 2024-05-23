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
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.repository.nok.safearea.SafeAreaRepositoryImpl
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaRVA
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SafeAreaViewModel @Inject constructor(
    private val repository: SafeAreaRepositoryImpl
) : ViewModel() {

    private val _dementiaKey = MutableStateFlow("")

    private val _safeAreaEvent = MutableSharedFlow<SafeAreaEvent>()
    val safeAreaEvent = _safeAreaEvent.asSharedFlow()

    val isSettingSafeArea = MutableStateFlow(false)
    sealed class SafeAreaEvent{
        data class FetchSafeArea(val safeAreas: List<SafeArea>): SafeAreaEvent()

        data class MapView(val behavior: Int, val coord: LatLng) : SafeAreaEvent()
    }

    fun setDementiaKey(dementiaKey: String) {
        _dementiaKey.value = dementiaKey
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
            repository.fetchSafeAreaAll(_dementiaKey.value).onSuccess {
                val list = mutableListOf<SafeArea>()
                it.safeAreaList.forEach { safeAreaList ->
                    val temp = if (safeAreaList.groupName == "notGrouped") {
                        safeAreaList.safeAreas.map { safeArea ->
                            SafeArea(
                                "",
                                safeAreaList.groupKey,
                                safeArea.areaKey,
                                safeArea.areaName,
                                safeArea.latitude,
                                safeArea.longitude,
                                safeArea.radius,
                                SafeAreaRVA.SAFE_AREA
                            )
                        }
                    } else {
                        safeAreaList.safeAreas.map {
                            SafeArea(
                                safeAreaList.groupName,
                                safeAreaList.groupKey,
                                "",
                                "",
                                0.0,
                                0.0,
                                0,
                                SafeAreaRVA.SAFE_AREA_GROUP
                            )
                        }
                    }
                    list.addAll(temp)
                }
                list.sortWith(
                    compareBy(
                        {it.viewType},
                        {it.groupName},
                        {it.areaName}
                    )
                )
                eventSafeArea(SafeAreaEvent.FetchSafeArea(list))
                Log.d("safeArea List", list.toString())
                Log.d("fetchSafeArea", it.toString())
            }
        }
    }

    fun fetchSafeAreaGroup(groupKey: String){
        viewModelScope.launch {
            repository.fetSafeAreaGroup(_dementiaKey.value, groupKey).onSuccess {
                Log.d("fetchSafeAreaGroup", it.toString())
            }
        }
    }
}