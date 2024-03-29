package kr.ac.tukorea.whereareu.data.api.nok

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NokHomeService {
    @GET("send-live-location-info")
    suspend fun getDementiaLocationInfo(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<LocationInfoResponse>>

    @GET("send-meaningful-location-info")
    suspend fun getMeaningfulPlace(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<MeaningfulPlaceResponse>>
}