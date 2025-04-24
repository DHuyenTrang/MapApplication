package com.example.mapapplication.repository

import com.example.mapapplication.data.request.UserRequest
import com.example.mapapplication.data.response.RefreshTokenResponse
import com.example.mapapplication.data.response.UserResponse
import com.example.mapapplication.network.APIService
import retrofit2.Response

class AuthRepository(
    private val apiService: APIService,
) {
    suspend fun login(phoneNumber: String, password: String): Response<UserResponse> {
        return apiService.login(UserRequest(phoneNumber, password))
    }

    suspend fun refreshToken(refresh_token: String): Response<RefreshTokenResponse> {
        return apiService.refreshToken(refresh_token)
    }
}