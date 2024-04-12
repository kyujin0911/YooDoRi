package kr.ac.tukorea.whereareu.data.repository.kakao

import kr.ac.tukorea.whereareu.data.model.kakao.address.AddressResponse
import kr.ac.tukorea.whereareu.data.model.kakao.keyword.KeywordResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface KakaoRepository {
    suspend fun getAddress(x: String, y: String): NetworkResult<AddressResponse>

    suspend fun searchWithKeyword(query: String, x: String, y: String, radius: Int): NetworkResult<KeywordResponse>
}