package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.ac.tukorea.whereareu.data.repository.nok.meaningfulplace.MeaningfulPlaceRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.domain.home.PredictLocation
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import javax.inject.Inject

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

    private val _predictEvent = MutableSharedFlow<NokHomeViewModel.PredictEvent>()
    val predictEvent = _predictEvent.asSharedFlow()

    private val _navigateEvent = MutableSharedFlow<NokHomeViewModel.NavigateEvent>()
    val navigateEvent = _navigateEvent.asSharedFlow()

    val navigateEventToString = MutableStateFlow(NokHomeViewModel.NavigateEvent.Home.toString())

    private val _tempMeaningfulPlace = MutableStateFlow<List<MeaningfulPlaceInfo>>(emptyList())

    val tempPredictLocation = MutableStateFlow<PredictLocation>(PredictLocation())

    private val _meaningfulPlace = MutableSharedFlow<List<MeaningfulPlaceInfo>>()
    val meaningfulPlace = _meaningfulPlace.asSharedFlow()


    sealed class PredictEvent{
        data class MeaningfulPlaceForPage(
            val meaningfulPlaceForListForPage: List<MeaningfulPlaceInfo>
        ): PredictEvent()

    }

    data class SearchNearbyPoliceStationForPage(val policeStationList: List<PoliceStationInfo>):
            PredictEvent()


}