package kr.ac.tukorea.whereareu.data.repository.nok.home

import kr.ac.tukorea.whereareu.data.model.home.GetLocationInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface NokHomeRepository {
    suspend fun getDementiaLocationInfo(nokKey: String): NetworkResult<GetLocationInfoResponse>
}