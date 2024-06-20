package kr.ac.tukorea.whereareu.di

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.tukorea.whereareu.data.repository.nok.home.NokHomeRepositoryImpl

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FCMServiceEntryPoint {
    fun nokHomeRepository(): NokHomeRepositoryImpl
}