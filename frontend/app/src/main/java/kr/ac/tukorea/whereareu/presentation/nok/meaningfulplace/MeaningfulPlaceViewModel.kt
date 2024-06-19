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

    private val _isMeaningful = MutableStateFlow(true)
    val isMeaningful = _isMeaningful.asStateFlow()


    private val _dementiaKey = MutableStateFlow("")
    private val _nokKey = MutableStateFlow("")

    private val _meaningfulEvent = MutableSharedFlow<MeaningfulEvent>()
    val meaningEvent = _meaningfulEvent.asSharedFlow()

    private val _tempMeaningfulPlace = MutableStateFlow<List<MeaningfulPlaceInfo>>(emptyList())

    val tempPredictLocation = MutableStateFlow<PredictLocation>(PredictLocation())

    private val _meaningfulPlace = MutableSharedFlow<List<MeaningfulPlaceInfo>>()
    val meaningfulPlace = _meaningfulPlace.asSharedFlow()


    sealed class MeaningfulEvent{
        data class StartMeaningful(val isMeaningful: Boolean) : MeaningfulEvent()

        data class MeaningfulPlaceForPage(
            val firstLatLng: LatLng,
            val meaningfulPlaceForListForPage: List<MeaningfulPlaceInfo>
        ): MeaningfulEvent()

        data class SearchNearbyPoliceStationForPage(val policeStationList: List<PoliceStationInfo>):
            MeaningfulEvent()

        data object PredictDone : MeaningfulEvent()

        data class MapView(val behavior: Int, val coord: LatLng) : MeaningfulEvent()

        data class StopPredict(val isPredicted: Boolean) : MeaningfulEvent()

    }
    fun eventMeaningful(event: MeaningfulEvent){
        viewModelScope.launch {
            _meaningfulEvent.emit(event)
        }
    }

    fun eventMeaningfulPlaceForPage(){
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
            _isMeaningful.emit(isPredicted)
            if (isPredicted) {
                eventMeaningful(MeaningfulEvent.StartMeaningful(isPredicted))
            } else {
                eventMeaningful(MeaningfulEvent.StopPredict(isPredicted))
            }
        }
    }

    fun meaningful(){
        viewModelScope.launch{
            val time = measureTimeMillis{
                async{getMeaningfulPlaces()}
                eventMeaningful(MeaningfulEvent.PredictDone)
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
                eventMeaningful(MeaningfulEvent.SearchNearbyPoliceStationForPage(policeStationInfo))
                meaningfulPlace.toModel(policeStationInfo)
            }
            val firstLatLng = meaningfulPlaceInfo.first().latLng
            eventMeaningful(MeaningfulEvent.MeaningfulPlaceForPage(firstLatLng, meaningfulPlaceInfo))
            _tempMeaningfulPlace.value = meaningfulPlaceInfo
            _meaningfulPlace.emit(meaningfulPlaceInfo)
        }.onException {
            Log.d("$tag error", it.toString())
        }
    }
}