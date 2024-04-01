package kr.ac.tukorea.whereareu.data.api

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.setting.GetUserInfoResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface UserInfoService {
    @GET("get-user-info")
    suspend fun getUserInfo(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<GetUserInfoResponse>>
}