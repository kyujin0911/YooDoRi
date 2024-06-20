package kr.ac.tukorea.whereareu.presentation.nok

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.ac.tukorea.whereareu.R

class NokMainViewModel: ViewModel() {
    private val _currentNavigationDestination = MutableStateFlow(R.id.nokHomeFragment)
    val currentNavigationDestination = _currentNavigationDestination.asStateFlow()

    fun setCurrentNavigationDestination(destination: Int){
        _currentNavigationDestination.value = destination
    }
}