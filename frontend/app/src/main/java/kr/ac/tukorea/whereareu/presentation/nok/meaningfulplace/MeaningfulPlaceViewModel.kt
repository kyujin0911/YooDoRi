package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

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
import kr.ac.tukorea.whereareu.data.repository.nok.meaningfulplace.MeaningfulPlaceRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.domain.home.PredictLocation
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class MeaningfulPlaceViewModel @Inject constructor(
    private val meaningfulPlaceRepository: MeaningfulPlaceRepositoryImpl
): ViewModel() {

    private var tag = "MeaningfulPlaceViewModel"

    private val _isPredicted = MutableStateFlow(true)
    val isPredicted = _isPredicted.asStateFlow()

    private val _isPredictDone = MutableStateFlow(false)

    private val _dementiaKey = MutableStateFlow("")
    private val _nokKey = MutableStateFlow("")

    private val _predictEvent = MutableSharedFlow<MeaningfulPlaceViewModel.PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

//    private val _navigateEvent = MutableSharedFlow<MeaningfulPlaceViewModel.NavigateEvent>()
//    val navigateEvent = _navigateEvent.asSharedFlow()

    val navigateEventToString = MutableStateFlow(NokHomeViewModel.NavigateEvent.Home.toString())

    private val _tempMeaningfulPlace = MutableStateFlow<List<MeaningfulPlaceInfo>>(emptyList())

    val tempPredictLocation = MutableStateFlow<PredictLocation>(PredictLocation())

    private val _meaningfulPlace = MutableSharedFlow<List<MeaningfulPlaceInfo>>()
    val meaningfulPlace = _meaningfulPlace.asSharedFlow()


    sealed class PredictEvent{
        data class StartPredict(val isPredicted: Boolean) : PredictEvent()

        data class MeaningfulPlaceForPage(
            val meaningfulPlaceForListForPage: List<MeaningfulPlaceInfo>
        ): PredictEvent()

        data class SearchNearbyPoliceStationForPage(val policeStationList: List<PoliceStationInfo>):
            PredictEvent()

        data object PredictDone : PredictEvent()

        data class MapView(val behavior: Int, val coord: LatLng) : PredictEvent()

        data class StopPredict(val isPredicted: Boolean) : PredictEvent()

    }
    fun eventPredict(event: PredictEvent){
        viewModelScope.launch {
            _predictEvent.emit(event)
        }
    }

    fun setIsPredictDone(isPredictDone: Boolean){
        _isPredictDone.value = isPredictDone
    }

    fun eventHomeState(){
        viewModelScope.launch{
            if(_tempMeaningfulPlace.value.isEmpty()){
                Log.d("$tag eventMeaningfulPlace", "_meaningfulPlace isEmpty")
                return@launch
            }
            _meaningfulPlace.emit(_tempMeaningfulPlace.value)
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
                eventPredict(PredictEvent.StartPredict(isPredicted))
            } else {
                eventPredict(PredictEvent.StopPredict(isPredicted))
            }
        }
    }

    fun predict(){
        viewModelScope.launch{
            val time = measureTimeMillis{
                async{getMeaningfulPlaces()}
                eventPredict(PredictEvent.PredictDone)
            }
        }
    }

    private suspend fun getMeaningfulPlaces() {
        meaningfulPlaceRepository.getMeaningfulPlaceForPage(_dementiaKey.value).onSuccess { response ->
            Log.d("$tag getMeaningfulPlaces", response.toString())
            val meaningfulPlaceInfo = response.meaningfulPlaces.map { meaningfulPlace ->
                val policeStationInfo = meaningfulPlace.policeStationInfo.map { policeStation ->
                    policeStation.toModel()
                }
                eventPredict(PredictEvent.SearchNearbyPoliceStationForPage(policeStationInfo))
                meaningfulPlace.toModel(policeStationInfo)
            }

            eventPredict(PredictEvent.MeaningfulPlaceForPage(meaningfulPlaceInfo))
            _tempMeaningfulPlace.value = meaningfulPlaceInfo
            _meaningfulPlace.emit(meaningfulPlaceInfo)
        }.onException {
            Log.d("$tag error", it.toString())
        }
    }
}