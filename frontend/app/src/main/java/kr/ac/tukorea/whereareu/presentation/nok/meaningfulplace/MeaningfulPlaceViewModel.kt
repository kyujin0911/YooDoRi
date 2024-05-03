package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.data.repository.nok.meaningfulPlace.NokMeaningfulPlaceRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class MeaningfulPlaceViewModel @Inject constructor(
    private val nokMeaningfulPlaceRepository: NokMeaningfulPlaceRepositoryImpl
) : ViewModel() {

//    val meaningfulPlaceList : List<MeaningfulPlaceInfo> = mutableListOf()

    private val _userMeaningfulPlaceList = MutableSharedFlow<List<MeaningfulPlace>>()
    val userMeaningfulPlaceList = _userMeaningfulPlaceList.asSharedFlow()

    fun getMeaningfulPlaces(dementiaKey: String) {
        viewModelScope.launch {
            nokMeaningfulPlaceRepository.getUserMeaningfulPlace("253050")
                .onSuccess { response -> // MeaningfulPlaceResponse
                    Log.d("getUserMeaningfulPlace", response.toString())
                    _userMeaningfulPlaceList.emit(response.meaningfulPlaces)
                }
                .onException {
                    Log.d("error", it.toString())
                }.onError {
                    Log.d("Error", it.toString())
                }
        }
    }
}