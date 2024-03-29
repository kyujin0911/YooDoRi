package kr.ac.tukorea.whereareu.data.repository.nok.home

import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.nok.home.DementiaAverageSpeedResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface NokHomeRepository {
    suspend fun getDementiaLocationInfo(dementiaKey: String): NetworkResult<LocationInfoResponse>

    suspend fun getMeaningfulPlace(dementiaKey: String): NetworkResult<MeaningfulPlaceResponse>

    suspend fun getDementiaLastInfo(request: DementiaKeyRequest): NetworkResult<DementiaAverageSpeedResponse>
}