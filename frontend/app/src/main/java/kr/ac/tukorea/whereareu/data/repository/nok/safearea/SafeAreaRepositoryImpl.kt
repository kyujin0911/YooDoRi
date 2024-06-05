package kr.ac.tukorea.whereareu.data.repository.nok.safearea

import kr.ac.tukorea.whereareu.data.api.nok.SafeAreaService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetCoordRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaGroupResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.GetSafeAreaResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaGroupRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaGroupResponse
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class SafeAreaRepositoryImpl @Inject constructor(
    private val api: SafeAreaService
): SafeAreaRepository {
    override suspend fun registerSafeArea(request: RegisterSafeAreaRequest): NetworkResult<RegisterSafeAreaResponse> {
        return handleApi({api.registerSafeArea(request)}) {response: RegisterSafeAreaResponse -> response}
    }

    override suspend fun registerSafeAreaGroup(request: RegisterSafeAreaGroupRequest): NetworkResult<RegisterSafeAreaGroupResponse> {
        return handleApi({api.registerSafeAreaGroup(request)}) {response: ResponseBody<RegisterSafeAreaGroupResponse> -> response.result}
    }

    override suspend fun fetchSafeAreaAll(dementiaKey: String): NetworkResult<GetSafeAreaResponse> {
        return handleApi({api.getSafeAreaAll(dementiaKey)}) { response: ResponseBody<GetSafeAreaResponse> -> response.result}
    }

    override suspend fun fetchSafeAreaGroup(
        dementiaKey: String,
        groupKey: String
    ): NetworkResult<GetSafeAreaGroupResponse> {
        return handleApi({api.getSafeAreaGroup(dementiaKey, groupKey)}) {response: ResponseBody<GetSafeAreaGroupResponse> -> response.result}
    }

    override suspend fun fetchCoord(request: GetCoordRequest): NetworkResult<RegisterSafeAreaResponse> {
        return handleApi({api.getCoord(request)}) {response: ResponseBody<RegisterSafeAreaResponse> -> response.result}
    }
}