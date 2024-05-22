package kr.ac.tukorea.whereareu.util.network

import android.util.Log
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.WhereAreUApplication
import kr.ac.tukorea.whereareu.util.AppConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

class NaverInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
        val newRequest = request().newBuilder()
            .addHeader("X-NCP-APIGW-API-KEY-ID", WhereAreUApplication.getString(R.string.naver_client_key))
            .addHeader("X-NCP-APIGW-API-KEY", WhereAreUApplication.getString(R.string.naver_client_secret_key))
            .build()

        proceed(newRequest)
    }
}
