package kr.ac.tukorea.whereareu.util.network

import android.util.Log
import kr.ac.tukorea.whereareu.util.AppConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

class NaverInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
        val newRequest = request().newBuilder()
            .addHeader("X-NCP-APIGW-API-KEY-ID", "nq32q75cmc")
            .addHeader("X-NCP-APIGW-API-KEY", "QRTGSGeI8H8Lwa8VtDweNbfCBbAneU5yvJqG8Awg")
            .build()

        proceed(newRequest)
    }
}
