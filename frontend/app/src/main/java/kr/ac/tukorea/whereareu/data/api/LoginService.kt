package kr.ac.tukorea.whereareu.data.api

import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.login.request.CheckInterConnectRequest
import kr.ac.tukorea.whereareu.data.model.login.response.CheckInterConnectResponse
import kr.ac.tukorea.whereareu.data.model.login.request.DementiaIdentityRequest
import kr.ac.tukorea.whereareu.data.model.login.response.DementiaIdentityResponse
import kr.ac.tukorea.whereareu.data.model.login.request.NokIdentityRequest
import kr.ac.tukorea.whereareu.data.model.login.response.NokIdentityResponse
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("receive-nok-info")
    suspend fun postNokIdentity(@Body request: NokIdentityRequest): Response<ResponseBody<NokIdentityResponse>>

    @POST("receive-dementia-info")
    suspend fun postDementiaIdentity(@Body request: DementiaIdentityRequest): Response<ResponseBody<DementiaIdentityResponse>>

    @POST("is-connected")
    suspend fun postIsConnected(@Body request: DementiaKeyRequest): Response<ResponseBody<CheckInterConnectResponse>>
}