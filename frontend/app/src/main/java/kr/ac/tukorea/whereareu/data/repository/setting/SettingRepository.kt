package kr.ac.tukorea.whereareu.data.repository.setting

import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.StateResponse
import kr.ac.tukorea.whereareu.data.model.setting.UpdateRateRequest
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface SettingRepository {
    suspend fun sendModifyUserInfo(request:ModifyUserInfoRequest): NetworkResult<StateResponse>

    suspend fun fetchUserInfo(nokKey: String): NetworkResult<GetUserInfoResponse>

    suspend fun sendUpdateRate(request: UpdateRateRequest): NetworkResult<StateResponse>
}