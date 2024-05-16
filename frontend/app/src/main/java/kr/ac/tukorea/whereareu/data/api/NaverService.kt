package kr.ac.tukorea.whereareu.data.api

import android.provider.Contacts.SettingsColumns.KEY
import kr.ac.tukorea.whereareu.data.model.naver.ReverseGeocodingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface NaverService {
    @GET("map-reversegeocode/v2/gc")
    suspend fun getReverseGeocodingInfo(
        @Query("coords") coords: String,
        @Query("orders") orders: String,
        @Query("output") output: String
    ): Response<ReverseGeocodingResponse>
}