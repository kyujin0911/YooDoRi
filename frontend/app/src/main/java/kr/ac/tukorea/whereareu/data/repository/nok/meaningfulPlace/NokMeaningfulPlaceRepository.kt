package kr.ac.tukorea.whereareu.data.repository.nok.meaningfulPlace

import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlaceResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface NokMeaningfulPlaceRepository {
    suspend fun getUserMeaningfulPlace(dementiaKey:String): NetworkResult<MeaningfulPlaceResponse>
}