package kr.ac.tukorea.whereareu.data.repository.dementia.home

import kr.ac.tukorea.whereareu.data.model.dementia.home.LocationInfo
import kr.ac.tukorea.whereareu.data.model.dementia.home.PostLocationInfoResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface DementiaHomeRepository {
    suspend fun postLocationInfo(request: LocationInfo): NetworkResult<PostLocationInfoResponse>
}