package kr.ac.tukorea.whereareu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.tukorea.whereareu.data.api.LoginService
import kr.ac.tukorea.whereareu.data.api.NaverService
import kr.ac.tukorea.whereareu.data.api.dementia.DementiaHomeService
import kr.ac.tukorea.whereareu.data.api.nok.NokHomeService
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
}