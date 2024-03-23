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
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoResponse
import kr.ac.tukorea.whereareu.data.repository.setting.SettingRepositoryImpl
import kr.ac.tukorea.whereareu.util.network.onSuccess
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    val repository: SettingRepositoryImpl
) : ViewModel() {

//    private val _updateUserName = MutableStateFlow<String>
//    val updateUserName = _updateUserName.asStateFlow()
    private val _updateUserName = MutableSharedFlow<Int>()
    val updateUserName = _updateUserName.asSharedFlow()



    fun setUpdateUserName(request: ModifyUserInfoRequest){
        viewModelScope.launch(Dispatchers.IO){
            repository.sendModifyUserInfo(request).onSuccess {
                Log.d("UpdateUserName","UserNameChanged")
                _updateUserName.emit(it.result)
            }
        }
    }
}