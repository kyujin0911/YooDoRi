package kr.ac.tukorea.whereareu.data.repository.kakao

import kr.ac.tukorea.whereareu.data.model.kakao.AddressResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface KakaoRepository {
    suspend fun getAddress(x: String, y: String): NetworkResult<AddressResponse>
}