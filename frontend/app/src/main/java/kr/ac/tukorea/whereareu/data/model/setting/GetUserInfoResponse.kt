package kr.ac.tukorea.whereareu.data.model.setting

import kr.ac.tukorea.whereareu.domain.login.userinfo.GetUserInfoResult

data class GetUserInfoResponse(
    val message: String,
    val result:GetUserInfoResult,
    val status : String
)
