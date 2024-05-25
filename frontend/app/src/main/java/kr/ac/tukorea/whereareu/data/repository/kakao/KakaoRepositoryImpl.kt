package kr.ac.tukorea.whereareu.data.repository.kakao

import kr.ac.tukorea.whereareu.data.api.KakaoService
import kr.ac.tukorea.whereareu.data.model.kakao.address.AddressResponse
import kr.ac.tukorea.whereareu.data.model.kakao.keyword.KeywordResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class KakaoRepositoryImpl @Inject constructor(
    private val api: KakaoService
): KakaoRepository {
    override suspend fun getAddress(x: String, y: String): NetworkResult<AddressResponse> {
        return handleApi({api.getAddress(x, y)}) {response: AddressResponse -> response}
    }

    override suspend fun searchWithKeyword(keyword: String
    ): NetworkResult<KeywordResponse> {
        return handleApi({api.searchPoliceStationNearby(keyword)}) {response: KeywordResponse -> response}
    }
}