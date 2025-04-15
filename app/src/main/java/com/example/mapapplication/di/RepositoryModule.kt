package com.example.mapapplication.di

import com.example.mapapplication.repository.AuthRepository
import com.example.mapapplication.repository.LocationSearchRepository
import com.example.mapapplication.repository.RouteRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get()) }
    single { RouteRepository(get()) }
    single { LocationSearchRepository(get()) }

}