package kr.ac.tukorea.whereareu.data.repository.naver

import kr.ac.tukorea.whereareu.data.model.naver.ReverseGeocodingResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import retrofit2.http.Query

interface NaverRepository {
    suspend fun getReverseGeocodingInfo(coords: String, orders: String, output: String): NetworkResult<ReverseGeocodingResponse>
}