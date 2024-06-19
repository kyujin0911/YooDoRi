package kr.ac.tukorea.whereareu.data.api.nok

import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NokMeaningfulPlaceService {
    @GET("locatoins/meaningful")
    suspend fun getUserMeaningfulPlace(@Query("dementiaKey") dementiaKey:String): Response<ResponseBody<MeaningfulPlaceResponse>>
}