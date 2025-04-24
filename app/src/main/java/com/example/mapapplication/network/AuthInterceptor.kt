package com.example.mapapplication.network

import android.util.Log
import com.example.mapapplication.manager.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val apiService: APIService,
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
        if (response.code == 401) {
            val newToken = refreshToken()
            if (newToken != null) {
                response.close()
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }
            else {
                tokenManager.clearToken()
            }
        }
        return response
    }
    private fun refreshToken(): String? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return try {
            val response = runBlocking {
                apiService.refreshToken(refreshToken)
            }
            if (response.isSuccessful) {
                val newTokens = response.body()!!
                Log.d("AUTH", "New access token: ${newTokens.access_token}")
                tokenManager.saveToken(newTokens.access_token, newTokens.refresh_token)
                newTokens.access_token
            } else {
                tokenManager.clearToken()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}