package kr.ac.tukorea.whereareu.data.repository.setting

import kr.ac.tukorea.whereareu.data.api.ModifyUserInfoService
import kr.ac.tukorea.whereareu.data.api.UserInfoService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(
    private val api: ModifyUserInfoService,
    private val api2: UserInfoService
) : SettingRepository{
    override suspend fun sendModifyUserInfo(request: ModifyUserInfoRequest): NetworkResult<ModifyUserInfoResponse> {
        return handleApi({ api.postModifyUserInfo(request) }) { response: ResponseBody<ModifyUserInfoResponse> -> response.result }
    }
    override suspend fun getUserInfo(dementiaKey: String): NetworkResult<GetUserInfoResponse> {
        return handleApi({api2.getUserInfo(dementiaKey)}) {response: ResponseBody<GetUserInfoResponse> -> response.result}
    }
}