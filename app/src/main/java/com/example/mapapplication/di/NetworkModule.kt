package com.example.mapapplication.di

import com.example.mapapplication.network.APIService
import com.example.mapapplication.network.AuthInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api-prod.gofa.vn"

val networkModule = module {

    single { AuthInterceptor(get()) }

    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // kết nối tối đa 30s
            .readTimeout(30, TimeUnit.SECONDS)    // đọc dữ liệu tối đa 30s
            .writeTimeout(30, TimeUnit.SECONDS)   // ghi dữ liệu tối đa 30s
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
    single { get<Retrofit>().create(APIService::class.java) }
}