package com.example.mapapplication.di

import com.example.mapapplication.manager.TokenManager
import com.example.mapapplication.network.APIService
import com.example.mapapplication.network.AuthInterceptor
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api-prod.gofa.vn"

val networkModule = module {
    // Provide OkHttpClient without AuthInterceptor for refresh token calls
    single(named("baseOkHttpClient")) {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Provide Retrofit for refresh token API (no AuthInterceptor)
    single(named("refreshRetrofit")) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get(named("baseOkHttpClient")))
            .build()
    }

    // Provide APIService for refresh token calls
    single<APIService>(named("refreshApiService")) {
        get<Retrofit>(named("refreshRetrofit")).create(APIService::class.java)
    }

    // Provide AuthInterceptor
    single {
        AuthInterceptor(
            tokenManager = get(),
            apiService = get(named("refreshApiService"))
        )
    }

    // Provide OkHttpClient with AuthInterceptor for regular API calls
    single(named("authOkHttpClient")) {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }

    // Provide Retrofit for regular API calls
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get(named("authOkHttpClient")))
            .build()
    }

    // Provide APIService for regular API calls
    single<APIService> {
        get<Retrofit>().create(APIService::class.java)
    }
}