package kr.ac.tukorea.whereareu.data.repository.naver

import kr.ac.tukorea.whereareu.data.api.NaverService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.naver.ReverseGeocodingResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class NaverRepositoryImpl @Inject constructor(
    val api: NaverService
): NaverRepository {
    override suspend fun getReverseGeocodingInfo(
        coords: String,
        orders: String,
        output: String
    ): NetworkResult<ReverseGeocodingResponse> {
        return handleApi({api.getReverseGeocodingInfo(coords, orders, output)}) {response: ReverseGeocodingResponse -> response}
    }
}