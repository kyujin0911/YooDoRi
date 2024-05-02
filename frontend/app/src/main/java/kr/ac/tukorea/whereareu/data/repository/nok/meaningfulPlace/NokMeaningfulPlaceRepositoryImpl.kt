package kr.ac.tukorea.whereareu.data.repository.nok.meaningfulPlace

import kr.ac.tukorea.whereareu.data.api.nok.NokMeaningfulPlaceService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class NokMeaningfulPlaceRepositoryImpl @Inject constructor(
    private val api: NokMeaningfulPlaceService
) : NokMeaningfulPlaceRepository{
    override suspend fun getMeaningfulPlace(dementiaKey: String): NetworkResult<MeaningfulPlaceResponse> {
        return handleApi({api.getMeaningfulPlace(dementiaKey)}) {response: ResponseBody<MeaningfulPlaceResponse> -> response.result}
    }
}