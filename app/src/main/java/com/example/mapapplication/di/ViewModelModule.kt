package com.example.mapapplication.di

import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.example.mapapplication.ui.auth.AuthViewModel
import com.example.mapapplication.ui.detail.LocationDetailViewModel
import com.example.mapapplication.ui.search.SearchLocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { CurrentLocationViewModel() }

    viewModel { AuthViewModel(get(), get()) }

    viewModel { RouteViewModel(get(), get()) }

    viewModel { SearchLocationViewModel(get()) }

    viewModel { LocationDetailViewModel(get()) }
}