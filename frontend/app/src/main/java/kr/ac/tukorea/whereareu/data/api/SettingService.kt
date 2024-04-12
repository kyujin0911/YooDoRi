package kr.ac.tukorea.whereareu.data.api

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.data.model.setting.StateResponse
import kr.ac.tukorea.whereareu.data.model.setting.UpdateRateRequest
import kr.ac.tukorea.whereareu.domain.login.userinfo.GetUserInfoResult
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface SettingService {
    @POST("users/modification/userInfo")
    suspend fun postModifyUserInfo(@Body request : ModifyUserInfoRequest): Response<ResponseBody<StateResponse>>

    @GET("users/info")
    suspend fun getUserInfo(@Query("nokKey") nokKey: String): Response<ResponseBody<GetUserInfoResult>>

    @POST("users/modification/updateRate")
    suspend fun postUpdateRate(@Body request : UpdateRateRequest): Response<ResponseBody<StateResponse>>
}