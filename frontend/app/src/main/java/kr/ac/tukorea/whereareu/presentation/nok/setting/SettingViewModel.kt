package kr.ac.tukorea.whereareu.presentation.nok.setting

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
import kr.ac.tukorea.whereareu.data.model.setting.UpdateRateRequest
import kr.ac.tukorea.whereareu.data.repository.setting.SettingRepositoryImpl
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.util.network.onError
import kr.ac.tukorea.whereareu.util.network.onException
import kr.ac.tukorea.whereareu.util.network.onFail
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: SettingRepositoryImpl
) : ViewModel() {

    private val _userInfo = MutableStateFlow<GetUserInfoResponse>(GetUserInfoResponse())
    val userInfo = _userInfo.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _updateRate = MutableStateFlow<String>("1")
    val updateRate = _updateRate.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun setUpdateRate(time:String){
        _updateRate.value = time
    }

    fun sendUpdateUserInfo(request: ModifyUserInfoRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendModifyUserInfo(request).onSuccess {response ->
                Log.d("UpdateUserInfo", "UserInfoChanged")
                _toastEvent.emit("정보가 변경되었습니다.")
            }
        }
    }

    fun sendUpdateOtherUserInfo(request: ModifyUserInfoRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendModifyUserInfo(request).onSuccess {response ->
                Log.d("UpdateOtherUserInfo", "OtherUserInfoChanged")
                _toastEvent.emit("정보가 변경되었습니다.")
            }
        }
    }
    fun getUserInfo(nokKey: String){
        viewModelScope.launch{
            repository.getUserInfo(nokKey).onSuccess {
                _userInfo.value = it
                //_name.value = it.nokInfoRecord.nokName
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

    fun sendUpdateTime(key: String, isDementia: Int){
        viewModelScope.launch(Dispatchers.IO){
            repository.sendUpdateRate(
                UpdateRateRequest(key, isDementia, _updateRate.value.toInt())
            ).onSuccess {
                _toastEvent.emit("정보가 변경되었습니다.")
                Log.d("UpdateRate", "UpdateRateChanged")
            }
        }
    }
}