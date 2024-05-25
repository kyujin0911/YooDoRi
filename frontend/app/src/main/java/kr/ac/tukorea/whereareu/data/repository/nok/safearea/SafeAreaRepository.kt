package kr.ac.tukorea.whereareu.data.repository.nok.safearea

import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetCoordRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaGroupResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface SafeAreaRepository {
    suspend fun registerSafeArea(request: RegisterSafeAreaRequest): NetworkResult<RegisterSafeAreaResponse>
    suspend fun fetchSafeAreaAll(dementiaKey: String): NetworkResult<GetSafeAreaResponse>

    suspend fun fetchSafeAreaGroup(dementiaKey: String, groupKey: String): NetworkResult<GetSafeAreaGroupResponse>

    suspend fun fetchCoord(request: GetCoordRequest): NetworkResult<RegisterSafeAreaResponse>
}