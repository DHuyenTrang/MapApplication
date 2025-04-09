package com.example.mapapplication.di

import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.example.mapapplication.viewmodel.SignInViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { CurrentLocationViewModel() }

    viewModel { SignInViewModel(get(), get()) }

    viewModel { RouteViewModel(get(), get()) }
}