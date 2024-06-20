package kr.ac.tukorea.whereareu.data.repository.nok.meaningfulplace

import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface MeaningfulPlaceRepository {
    suspend fun getMeaningfulPlaceForPage(dementiaKey: String): NetworkResult<MeaningfulPlaceResponse>
}