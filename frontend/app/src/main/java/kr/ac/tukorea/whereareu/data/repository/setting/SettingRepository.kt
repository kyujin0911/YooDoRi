package kr.ac.tukorea.whereareu.data.repository.setting

import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.StateResponse
import kr.ac.tukorea.whereareu.domain.login.userinfo.GetUserInfoResult
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface SettingRepository {
    suspend fun sendModifyUserInfo(request:ModifyUserInfoRequest): NetworkResult<StateResponse>

    suspend fun getUserInfo(nokKey: String): NetworkResult<GetUserInfoResult>
}