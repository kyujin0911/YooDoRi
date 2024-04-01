package kr.ac.tukorea.whereareu.data.repository.userInfo

import kr.ac.tukorea.whereareu.data.api.UserInfoService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class UserInfoRepositoryImpl @Inject constructor(
    private val api: UserInfoService
) : UserInfoRepository{
    override suspend fun getUserInfo(dementiaKey: String): NetworkResult<GetUserInfoResponse> {
        return handleApi({api.getUserInfo(dementiaKey)}) {response: ResponseBody<GetUserInfoResponse> -> response.result}
    }
}