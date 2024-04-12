package kr.ac.tukorea.whereareu.data.api

import kr.ac.tukorea.whereareu.data.model.kakao.address.AddressResponse
import kr.ac.tukorea.whereareu.data.model.kakao.keyword.KeywordResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoService {
    @GET("geo/coord2address.json")
    suspend fun getAddress(@Query("x") x: String, @Query("y") y: String): Response<AddressResponse>

    @GET("search/keyword.json")
    suspend fun searchWithKeyword(
        @Query("query") query: String,
        @Query("x") x: String,
        @Query("y") y: String,
        @Query("radius") radius: Int
    ): Response<KeywordResponse>
}