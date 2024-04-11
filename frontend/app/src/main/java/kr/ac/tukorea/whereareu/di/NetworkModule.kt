package kr.ac.tukorea.whereareu.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.WhereAreUApplication
import kr.ac.tukorea.whereareu.data.api.dementia.DementiaHomeService
import kr.ac.tukorea.whereareu.data.api.LoginService
import kr.ac.tukorea.whereareu.data.api.nok.NokHomeService
import kr.ac.tukorea.whereareu.util.location.LocationService
import kr.ac.tukorea.whereareu.util.network.KakaoInterceptor
import kr.ac.tukorea.whereareu.util.network.NaverInterceptor
import okhttp3.Interceptor
import okhttp3.Interceptor.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BaseRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class NaverRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KaKaoRetrofit

    const val NETWORK_EXCEPTION_OFFLINE_CASE = "network status is offline"
    const val NETWORK_EXCEPTION_BODY_IS_NULL = "result.json body is null"

    @Provides
    @Singleton
    @BaseRetrofit
    fun provideOKHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val closeInterceptor = Interceptor { chain ->
            val request: Request =
                chain.request().newBuilder().addHeader("Connection", "close").build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addNetworkInterceptor(closeInterceptor)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @Singleton
    @NaverRetrofit
    fun provideNaverOKHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val closeInterceptor = Interceptor { chain ->
            val request: Request =
                chain.request().newBuilder().addHeader("Connection", "close").build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(NaverInterceptor())
            .addInterceptor(interceptor)
            .addNetworkInterceptor(closeInterceptor)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @Singleton
    @KaKaoRetrofit
    fun provideKakaoOKHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val closeInterceptor = Interceptor { chain ->
            val request: Request =
                chain.request().newBuilder().addHeader("Connection", "close").build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(KakaoInterceptor())
            .addInterceptor(interceptor)
            .addNetworkInterceptor(closeInterceptor)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @Singleton
    @BaseRetrofit
    fun provideRetrofit(
        @BaseRetrofit okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(WhereAreUApplication.getString(R.string. base_url))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @NaverRetrofit
    fun provideNaverRetrofit(
        @NaverRetrofit okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(WhereAreUApplication.getString(R.string. naver_url))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @KaKaoRetrofit
    fun provideKakaoRetrofit(
        @KaKaoRetrofit okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(WhereAreUApplication.getString(R.string. kakao_url))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLocationService(): LocationService{
        return LocationService()
    }
}