package kr.ac.tukorea.whereareu.data.api.nok

import kr.ac.tukorea.whereareu.data.model.CommonResponse
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetCoordRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaGroupResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaGroupRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SafeAreaService {
    @POST("safeArea/register")
    suspend fun registerSafeArea(@Body request: RegisterSafeAreaRequest): Response<RegisterSafeAreaResponse>

    @POST("safeArea/register/group")
    suspend fun registerSafeAreaGroup(@Body request: RegisterSafeAreaGroupRequest): Response<ResponseBody<RegisterSafeAreaResponse>>

    @GET("safeArea/info")
    suspend fun getSafeAreaAll(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<GetSafeAreaResponse>>

    @GET("safeArea/info/group")
    suspend fun getSafeAreaGroup(@Query("dementiaKey") dementiaKey: String, @Query("groupKey") groupKey: String): Response<ResponseBody<GetSafeAreaGroupResponse>>

    @POST("address/conversion")
    suspend fun getCoord(@Body request: GetCoordRequest): Response<ResponseBody<RegisterSafeAreaResponse>>
}