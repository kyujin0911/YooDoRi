package kr.ac.tukorea.whereareu.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.data.repository.userInfo.UserInfoRepository
import kr.ac.tukorea.whereareu.data.repository.userInfo.UserInfoRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    val repository: UserInfoRepositoryImpl
): ViewModel() {

    private val _userInfo = MutableSharedFlow<GetUserInfoResponse>()
    val userInfo =  _userInfo.asSharedFlow()

    fun getUserInfo(dementiaKey: String){
        viewModelScope.launch{
            repository.getUserInfo(dementiaKey).onSuccess {
                _userInfo.emit(it)
                Log.d("SettingViewModel", "getUserInfo Success")
            }
        }
    }
}