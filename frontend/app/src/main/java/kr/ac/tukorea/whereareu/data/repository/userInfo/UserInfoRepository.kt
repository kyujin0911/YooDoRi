package kr.ac.tukorea.whereareu.data.repository.userInfo

import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import kr.ac.tukorea.whereareu.domain.login.userinfo.GetUserInfoResult
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface UserInfoRepository {
    suspend fun getUserInfo(dementiaKey: String): NetworkResult<GetUserInfoResult>
}