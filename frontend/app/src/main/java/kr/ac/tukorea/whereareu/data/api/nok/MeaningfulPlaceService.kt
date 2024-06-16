package kr.ac.tukorea.whereareu.data.api.nok

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MeaningfulPlaceService {
    @GET("locations/meaningful")
    suspend fun getMeaningfulPlaceForPage(@Query("dementiaKey") dementiaKey: String): Response<ResponseBody<MeaningfulPlaceResponse>>

}