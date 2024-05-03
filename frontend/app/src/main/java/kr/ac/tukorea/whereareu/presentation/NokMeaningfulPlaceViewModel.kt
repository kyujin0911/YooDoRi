package kr.ac.tukorea.whereareu.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.repository.nok.meaningfulPlace.NokMeaningfulPlaceRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class NokMeaningfulPlaceViewModel @Inject constructor(
    private val nokMeaningfulPlaceRepository: NokMeaningfulPlaceRepositoryImpl
) : ViewModel() {

//    val meaningfulPlaceList : List<MeaningfulPlaceInfo> = mutableListOf()

    private val _userMeaningfulPlaceList : MutableList<MeaningfulPlaceInfo> = mutableListOf()
    val userMeaningfulPlaceList : List<MeaningfulPlaceInfo>
        get() = _userMeaningfulPlaceList

    fun getMeaningfulPlaces(dementiaKey : String) {
        viewModelScope.launch {
            nokMeaningfulPlaceRepository.getUserMeaningfulPlace(dementiaKey)
//            nokMeaningfulPlaceRepository.getUserMeaningfulPlace("253050")
//                .onSuccess {
//                Log.d("getUserMeaningfulPlace", "onSuccess"){
//                    _userMeaningfulPlaceList.addAll(it)
//                }
//            }
                .onSuccess { response -> // MeaningfulPlaceResponse
                    Log.d("getUserMeaningfulPlace", response.toString())
                    val meaningfulPlaceInfo = response.meaningfulLocations.map { meaningfulPlace ->
                        MeaningfulPlaceInfo(
                            meaningfulPlace.address,
                            meaningfulPlace.latitude,
                            meaningfulPlace.longitude,
                            false,
                        )
                        Log.d("MeaningfulPlaceViewModel", meaningfulPlace.address)
                    }
                }
                .onException {
                    Log.d("error", it.toString())
                }
        }
    }

}