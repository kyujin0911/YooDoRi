package kr.ac.tukorea.whereareu.data.api.nok

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaResponse
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

    @GET("safeArea/info")
    suspend fun getSafeArea(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<GetSafeAreaResponse>>
}