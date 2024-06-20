package kr.ac.tukorea.whereareu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.tukorea.whereareu.data.api.KakaoService
import kr.ac.tukorea.whereareu.data.api.LoginService
import kr.ac.tukorea.whereareu.data.api.NaverService
import kr.ac.tukorea.whereareu.data.api.SettingService
import kr.ac.tukorea.whereareu.data.api.dementia.DementiaHomeService
import kr.ac.tukorea.whereareu.data.api.nok.LocationHistoryService
import kr.ac.tukorea.whereareu.data.api.nok.MeaningfulPlaceService
import kr.ac.tukorea.whereareu.data.api.nok.NokHomeService
import kr.ac.tukorea.whereareu.data.api.nok.SafeAreaService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    private inline fun <reified T> Retrofit.buildService(): T {
        return this.create(T::class.java)
    }

    @Provides
    @Singleton
    fun provideLoginApi(@NetworkModule.BaseRetrofit retrofit: Retrofit): LoginService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideDementiaHomeApi(@NetworkModule.BaseRetrofit retrofit: Retrofit): DementiaHomeService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideNokHomeApi(@NetworkModule.BaseRetrofit retrofit: Retrofit): NokHomeService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideNaverApi(@NetworkModule.NaverRetrofit retrofit: Retrofit): NaverService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideKakaoApi(@NetworkModule.KaKaoRetrofit retrofit: Retrofit): KakaoService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideSettingAPI(@NetworkModule.BaseRetrofit retrofit: Retrofit): SettingService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideLocationHistoryAPI(@NetworkModule.BaseRetrofit retrofit: Retrofit): LocationHistoryService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideSafeAreaAPI(@NetworkModule.BaseRetrofit retrofit: Retrofit): SafeAreaService {
        return retrofit.buildService()
    }

    @Provides
    @Singleton
    fun provideNokMeaningfulPlaceApi(@NetworkModule.BaseRetrofit retrofit: Retrofit): MeaningfulPlaceService {
        return retrofit.buildService()
    }
}