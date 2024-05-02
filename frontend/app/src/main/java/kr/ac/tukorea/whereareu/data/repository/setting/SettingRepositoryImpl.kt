package kr.ac.tukorea.whereareu.data.repository.setting

import kr.ac.tukorea.whereareu.data.api.SettingService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.StateResponse
import kr.ac.tukorea.whereareu.data.model.setting.UpdateRateRequest
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(
    private val api: SettingService
) : SettingRepository{
    override suspend fun sendModifyUserInfo(request: ModifyUserInfoRequest): NetworkResult<StateResponse> {
        return handleApi({ api.postModifyUserInfo(request) }) { response: ResponseBody<StateResponse> -> response.result }
    }

    override suspend fun fetchUserInfo(nokKey: String): NetworkResult<GetUserInfoResponse> {
        return handleApi({api.getUserInfo(nokKey)}) {response: ResponseBody<GetUserInfoResponse> -> response.result}
    }

    override suspend fun sendUpdateRate(request: UpdateRateRequest): NetworkResult<StateResponse> {
        return handleApi({api.postUpdateRate(request)}){response: ResponseBody<StateResponse> -> response.result}
    }
}