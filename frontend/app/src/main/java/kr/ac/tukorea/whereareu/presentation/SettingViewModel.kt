package kr.ac.tukorea.whereareu.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.StateResponse
import kr.ac.tukorea.whereareu.data.model.setting.UpdateRateRequest
import kr.ac.tukorea.whereareu.data.repository.setting.SettingRepositoryImpl
import kr.ac.tukorea.whereareu.domain.login.userinfo.GetUserInfoResult
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: SettingRepositoryImpl
) : ViewModel() {
    private val _updateUserInfo = MutableSharedFlow<StateResponse>()
    val updateUserInfo = _updateUserInfo.asSharedFlow()

    private val _updateOtherUserInfo = MutableSharedFlow<StateResponse>()
    val updateOtherUserInfo = _updateOtherUserInfo.asSharedFlow()

    private val _userInfo = MutableSharedFlow<GetUserInfoResult>()
    val userInfo =  _userInfo.asSharedFlow()

    private val _settingTime = MutableStateFlow<String>("1")
    val settingTime = _settingTime.asStateFlow()
    // MutableSharedFlow()를 사용하면 bindind이 안됨 -> dataBinding을 하기위해서는 MutableStateFlow를 사용해야함

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun setSettingTime(time:String){
        _settingTime.value = time
    }

    fun sendUpdateUserInfo(request: ModifyUserInfoRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendModifyUserInfo(request).onSuccess {response ->
                Log.d("UpdateUserInfo", "UserInfoChanged")
                _updateOtherUserInfo.emit(response)
                _toastEvent.emit("정보가 변경되었습니다.")
            }
        }
    }

    fun sendUpdateOtherUserInfo(request: ModifyUserInfoRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendModifyUserInfo(request).onSuccess {
                Log.d("UpdateOtherUserInfo", "OtherUserInfoChanged")
                _updateOtherUserInfo.emit(StateResponse(it.message, it.status))
            }
        }
    }
    fun getUserInfo(nokKey: String){
        viewModelScope.launch{
            repository.getUserInfo(nokKey).onSuccess {
                _userInfo.emit(it)
                Log.d("SettingViewModel", "getUserInfo Success")
            }.onError {
                Log.d("error in SettingVIewModel", it.toString())
            }.onException {
                Log.d("exception in SettingVIewModel", it.toString())
            }.onFail {
                Log.d("fail in SettingVIewModel", it.toString())
            }
        }
    }

    fun sendUpdateTime(request: UpdateRateRequest){
        viewModelScope.launch(Dispatchers.IO){
            repository.sendUpdateRate(request).onSuccess {
                Log.d("UpdateRate", "UpdateRateChanged")
                _settingTime.emit(it.message)
            }
        }
    }
}