package kr.ac.tukorea.whereareu.data.repository.nok.safearea

import kr.ac.tukorea.whereareu.data.api.nok.SafeAreaService
import kr.ac.tukorea.whereareu.data.model.ResponseBody
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult
import kr.ac.tukorea.whereareu.util.network.handleApi
import javax.inject.Inject

class SafeAreaRepositoryImpl @Inject constructor(
    private val api: SafeAreaService
): SafeAreaRepository {
    override suspend fun registerSafeArea(request: RegisterSafeAreaRequest): NetworkResult<RegisterSafeAreaResponse> {
        return handleApi({api.registerSafeArea(request)}) {response: ResponseBody<RegisterSafeAreaResponse> -> response.result}
    }
}