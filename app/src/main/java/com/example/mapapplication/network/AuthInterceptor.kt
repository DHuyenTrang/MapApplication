package com.example.mapapplication.network

import android.util.Log
import com.example.mapapplication.manager.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = tokenManager.getAccessToken() ?: ""
        val userID = tokenManager.getUserId() ?: ""
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("x-id", userID)
            .build()
        val response = chain.proceed(request)

        if (response.code == 644) { // log out
            Log.d("AuthInterceptor", "Received 644 - logging out")
            tokenManager.logout()
        }
        return response
    }
//    private fun refreshToken(): String? {
//        val refreshToken = tokenManager.getRefreshToken() ?: return null
//
//        return try {
//            val response = apiService.getRefreshToken(RefreshTokenRequest(refreshToken)).execute()
//            if (response.isSuccessful) {
//                val newTokens = response.body()!!
//                Log.d("AUTH", "New access token: ${newTokens.access_token}")
//                tokenManager.saveToken(newTokens.access_token, newTokens.refresh_token)
//                newTokens.access_token
//            } else {
//                tokenManager.clearToken()
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
}