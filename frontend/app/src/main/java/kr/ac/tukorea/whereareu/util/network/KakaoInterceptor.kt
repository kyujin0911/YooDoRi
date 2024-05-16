package kr.ac.tukorea.whereareu.util.network

import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.WhereAreUApplication
import okhttp3.Interceptor
import okhttp3.Response

class KakaoInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
        val newRequest = request().newBuilder()
            .addHeader("Authorization", "KakaoAK ${WhereAreUApplication.getString(R.string.kakao_rest_key)}")
            .build()

        proceed(newRequest)
    }
}