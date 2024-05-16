package kr.ac.tukorea.whereareu.data.repository.nok.history

import kr.ac.tukorea.whereareu.data.api.nok.LocationHistoryService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryRequest
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class LocationHistoryRepositoryImpl @Inject constructor(
    private val api: LocationHistoryService
) : LocationHistoryRepository {
    override suspend fun fetchLocationHistory(date: String, dementiaKey: String): NetworkResult<LocationHistoryResponse> {
        return handleApi({ api.fetchLocationHistory(date, dementiaKey) }) { response: ResponseBody<LocationHistoryResponse> -> response.result }
    }
}