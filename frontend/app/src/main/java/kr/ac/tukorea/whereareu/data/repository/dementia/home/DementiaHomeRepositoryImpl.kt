package kr.ac.tukorea.whereareu.data.repository.dementia.home

import kr.ac.tukorea.whereareu.data.api.dementia.DementiaHomeService
import kr.ac.tukorea.whereareu.data.model.dementia.home.PostLocationInfoRequest
import kr.ac.tukorea.whereareu.data.model.dementia.home.PostLocationInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class DementiaHomeRepositoryImpl @Inject constructor(
    private val api: DementiaHomeService
): DementiaHomeRepository {
    override suspend fun postLocationInfo(request: PostLocationInfoRequest): NetworkResult<PostLocationInfoResponse> {
        return handleApi({api.postLocationInfo(request)}) {response: PostLocationInfoResponse -> response}
    }
}