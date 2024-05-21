package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.repository.nok.safearea.SafeAreaRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SafeAreaViewModel @Inject constructor(
    private val repository: SafeAreaRepositoryImpl
): ViewModel() {

    fun registerSafeArea(request: RegisterSafeAreaRequest){
        viewModelScope.launch {
            repository.registerSafeArea(request).onSuccess {
                Log.d("registerSafeArea", it.toString())
            }
        }
    }
}