package kr.ac.tukorea.whereareu.data.repository.nok.home

import kr.ac.tukorea.whereareu.data.api.nok.NokHomeService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class NokHomeRepositoryImpl @Inject constructor(
    private val api: NokHomeService
): NokHomeRepository {
    override suspend fun getDementiaLocationInfo(dementiaKey: String): NetworkResult<LocationInfoResponse> {
        return handleApi({api.getDementiaLocationInfo(dementiaKey)}) {response: ResponseBody<LocationInfoResponse> -> response.result}
    }

    override suspend fun getMeaningfulPlace(dementiaKey: String): NetworkResult<MeaningfulPlaceResponse> {
        return handleApi({api.getMeaningfulPlace(dementiaKey)}) {response: ResponseBody<MeaningfulPlaceResponse> -> response.result}
    }

}