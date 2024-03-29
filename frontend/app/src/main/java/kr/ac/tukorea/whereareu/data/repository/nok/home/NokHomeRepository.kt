package kr.ac.tukorea.whereareu.data.repository.nok.home

import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface NokHomeRepository {
    suspend fun getDementiaLocationInfo(dementiaKey: String): NetworkResult<LocationInfoResponse>
}