package kr.ac.tukorea.whereareu.data.api.nok

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NokHistoryService {
    @GET("locations/history")
    suspend fun fetchLocationHistory(
        @Query("date") date: String,
        @Query("dementiaKey") dementiaKey: String
    ): Response<ResponseBody<LocationHistoryResponse>>
}