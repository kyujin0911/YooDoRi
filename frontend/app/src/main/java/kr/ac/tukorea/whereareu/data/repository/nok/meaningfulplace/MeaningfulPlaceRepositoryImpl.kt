package kr.ac.tukorea.whereareu.data.repository.nok.meaningfulplace

import kr.ac.tukorea.whereareu.data.api.nok.MeaningfulPlaceService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class MeaningfulPlaceRepositoryImpl @Inject constructor(
    private val api: MeaningfulPlaceService
): MeaningfulPlaceRepository{
    override suspend fun getMeaningfulPlaceForPage(dementiaKey: String): NetworkResult<MeaningfulPlaceResponse> {
        return handleApi({api.getMeaningfulPlaceForPage(dementiaKey)}) {response: ResponseBody<MeaningfulPlaceResponse> -> response.result}
    }
}