package kr.ac.tukorea.whereareu.data.repository.dementia.home

import kr.ac.tukorea.whereareu.data.api.dementia.DementiaHomeService
import kr.ac.tukorea.whereareu.data.model.dementia.home.LocationInfo
import kr.ac.tukorea.whereareu.data.model.dementia.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class DementiaHomeRepositoryImpl @Inject constructor(
    private val api: DementiaHomeService
): DementiaHomeRepository {
    override suspend fun postLocationInfo(request: LocationInfo): NetworkResult<LocationInfoResponse> {
        return handleApi({api.postLocationInfo(request)}) {response: LocationInfoResponse -> response}
    }
}