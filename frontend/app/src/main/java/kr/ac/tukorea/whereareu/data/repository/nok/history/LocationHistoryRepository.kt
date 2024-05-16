package kr.ac.tukorea.whereareu.data.repository.nok.history

import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryRequest
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface LocationHistoryRepository {
    suspend fun fetchLocationHistory(date: String, dementiaKey: String): NetworkResult<LocationHistoryResponse>
}