package kr.ac.tukorea.whereareu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kr.ac.tukorea.whereareu.data.api.KakaoService
import kr.ac.tukorea.whereareu.data.api.dementia.DementiaHomeService
import kr.ac.tukorea.whereareu.data.api.LoginService
import kr.ac.tukorea.whereareu.data.api.NaverService
import kr.ac.tukorea.whereareu.data.api.nok.NokHomeService
import kr.ac.tukorea.whereareu.data.api.SettingService
import kr.ac.tukorea.whereareu.data.api.nok.LocationHistoryService
import kr.ac.tukorea.whereareu.data.repository.dementia.home.DementiaHomeRepository
import kr.ac.tukorea.whereareu.data.repository.dementia.home.DementiaHomeRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepository
import kr.ac.tukorea.whereareu.data.repository.kakao.KakaoRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.login.LoginRepository
import kr.ac.tukorea.whereareu.data.repository.login.LoginRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepository
import kr.ac.tukorea.whereareu.data.repository.naver.NaverRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.history.LocationHistoryRepository
import kr.ac.tukorea.whereareu.data.repository.nok.history.LocationHistoryRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepository
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl
import kr.ac.tukorea.whereareu.data.repository.setting.SettingRepository
import kr.ac.tukorea.whereareu.data.repository.setting.SettingRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @ViewModelScoped
    @Provides
    fun providesLoginRepository(
        loginService: LoginService
    ): LoginRepository = LoginRepositoryImpl(loginService)

    @ViewModelScoped
    @Provides
    fun providesDementiaHomeRepository(
        dementiaHomeService: DementiaHomeService
    ): DementiaHomeRepository = DementiaHomeRepositoryImpl(dementiaHomeService)

    @ViewModelScoped
    @Provides
    fun provideNokHomeRepository(
        nokHomeService: NokHomeService
    ): NokHomeRepository = NokHomeRepositoryImpl(nokHomeService)

    @ViewModelScoped
    @Provides
    fun provideNaverRepository(
        naverService: NaverService
    ): NaverRepository = NaverRepositoryImpl(naverService)

    @ViewModelScoped
    @Provides
    fun provideKakaoRepository(
        kakaoService: KakaoService
    ): KakaoRepository = KakaoRepositoryImpl(kakaoService)

    @ViewModelScoped
    @Provides
    fun providesSettingRepository(
        settingService: SettingService
    ): SettingRepository = SettingRepositoryImpl(settingService)

    @ViewModelScoped
    @Provides
    fun providesLocationHistoryRepository(
        locationHistoryService: LocationHistoryService
    ): LocationHistoryRepository = LocationHistoryRepositoryImpl(locationHistoryService)

}