package kr.ac.tukorea.whereareu.data.api

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoResponse
import kr.ac.tukorea.whereareu.domain.login.userinfo.GetUserInfoResult
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface SettingService {
    @POST("modify-user-info")
    suspend fun postModifyUserInfo(@Body request : ModifyUserInfoRequest): Response<ResponseBody<ModifyUserInfoResponse>>

    @GET("get-user-info")
    suspend fun getUserInfo(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<GetUserInfoResult>>
}