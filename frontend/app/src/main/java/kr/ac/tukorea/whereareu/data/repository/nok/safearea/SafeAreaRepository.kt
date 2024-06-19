package kr.ac.tukorea.whereareu.data.repository.nok.safearea

import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetCoordRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.response.GetSafeAreaGroupResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.response.GetSafeAreaResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaGroupRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.response.RegisterSafeAreaGroupResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.response.GetCoordResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.response.RegisterSafeAreaResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface SafeAreaRepository {
    suspend fun registerSafeArea(request: RegisterSafeAreaRequest): NetworkResult<RegisterSafeAreaResponse>

    suspend fun registerSafeAreaGroup(request: RegisterSafeAreaGroupRequest): NetworkResult<RegisterSafeAreaGroupResponse>
    suspend fun fetchSafeAreaAll(dementiaKey: String): NetworkResult<GetSafeAreaResponse>

    suspend fun fetchSafeAreaGroup(dementiaKey: String, groupKey: String): NetworkResult<GetSafeAreaGroupResponse>

    suspend fun fetchCoord(request: GetCoordRequest): NetworkResult<GetCoordResponse>
}