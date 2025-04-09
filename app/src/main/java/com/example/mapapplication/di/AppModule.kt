package com.example.mapapplication.di

import android.content.Context
import android.content.SharedPreferences
import com.example.mapapplication.TokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    includes(viewModelModule)
    includes(repositoryModule)
    includes(networkModule)

    single<SharedPreferences> { androidContext().getSharedPreferences("authPrefs", Context.MODE_PRIVATE) }
    single { TokenManager(androidContext()) }

}