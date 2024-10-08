package kr.ac.tukorea.whereareu.data.repository.login

import kr.ac.tukorea.whereareu.data.model.DementiaKeyRequest
import kr.ac.tukorea.whereareu.data.model.login.response.CheckInterConnectResponse
import kr.ac.tukorea.whereareu.data.model.login.request.DementiaIdentityRequest
import kr.ac.tukorea.whereareu.data.model.login.request.NokIdentityRequest
import kr.ac.tukorea.whereareu.data.model.login.request.UserLoginRequest
import kr.ac.tukorea.whereareu.data.model.login.response.DementiaIdentityResponse
import kr.ac.tukorea.whereareu.data.model.login.response.NokIdentityResponse
import kr.ac.tukorea.whereareu.data.model.setting.StateResponse
import kr.ac.tukorea.whereareu.util.network.NetworkResult

interface LoginRepository {
    suspend fun sendNokIdentity(request: NokIdentityRequest): NetworkResult<NokIdentityResponse>
    suspend fun sendDementiaIdentity(request: DementiaIdentityRequest): NetworkResult<DementiaIdentityResponse>
    suspend fun checkInterConnected(request: DementiaKeyRequest): NetworkResult<CheckInterConnectResponse>
    suspend fun sendUserLogin(request: UserLoginRequest): NetworkResult<StateResponse>
}