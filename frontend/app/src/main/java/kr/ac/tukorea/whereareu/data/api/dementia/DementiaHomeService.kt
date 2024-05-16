package kr.ac.tukorea.whereareu.data.api.dementia

import kr.ac.tukorea.whereareu.data.model.dementia.home.PostLocationInfoRequest
import kr.ac.tukorea.whereareu.data.model.dementia.home.PostLocationInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DementiaHomeService {
    @POST("locations/dementias")
    suspend fun postLocationInfo(
        @Body request: PostLocationInfoRequest
    ): Response<PostLocationInfoResponse>

}