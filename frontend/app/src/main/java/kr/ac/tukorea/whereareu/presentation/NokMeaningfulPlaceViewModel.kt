package kr.ac.tukorea.whereareu.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.ac.tukorea.whereareu.data.repository.nok.meaningfulPlace.NokMeaningfulPlaceRepositoryImpl
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class NokMeaningfulPlaceViewModel @Inject constructor(
    private val nokMeaningfulPlaceRepository: NokMeaningfulPlaceRepositoryImpl
) : ViewModel() {

//    private val _meaningfulPlaceList = List<MeaningfulPlaceInfo>()
    data class meaningfulPlace(val meaningfulPlaceList : List<MeaningfulPlaceInfo>)
     fun getUserMeaningfulPlace(){

    }

    private suspend fun getMeaningfulPlaces() {
        nokMeaningfulPlaceRepository.getUserMeaningfulPlace("253050").onSuccess { response -> //it: MeaningfulPlaceResponse
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
        }.onException {
            Log.d("error", it.toString())
        }
    }
}