package com.example.mapapplication.repository

import com.example.mapapplication.data.request.UserRequest
import com.example.mapapplication.data.response.UserResponse
import com.example.mapapplication.network.APIService
import retrofit2.Response

class AuthRepository(
    private val apiService: APIService,
) {
    suspend fun login(phoneNumber: String, password: String): Response<UserResponse> {
        return apiService.login(UserRequest(phoneNumber, password))
    }
}