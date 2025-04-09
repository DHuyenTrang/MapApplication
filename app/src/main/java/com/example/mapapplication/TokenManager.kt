package com.example.mapapplication

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TokenManager (context: Context) {
    private val authPrefs: SharedPreferences = context.getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
    private val userPrefs: SharedPreferences = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)

    fun saveToken(accessToken: String, refreshToken: String) {
        authPrefs.edit().apply {
            putString("accessToken", accessToken)
            putString("refreshToken", refreshToken)
            apply()
            Log.d("TokenManager", "saveToken: $accessToken, $refreshToken")
        }
    }

    fun saveUserId(userId: String) {
        userPrefs.edit().apply {
            putString("user_id", userId)
            apply()
        }
    }

    fun getUserId(): String? {
        return userPrefs.getString("user_id", null)
    }

    fun getAccessToken(): String? {
        return authPrefs.getString("accessToken", null)
    }

    fun getRefreshToken(): String? {
        return authPrefs.getString("refreshToken", null)
    }

    fun clearToken() {
        authPrefs.edit().remove("accessToken")
            .remove("refreshToken").apply()
        userPrefs.edit().remove("user_id").apply()

        Log.d("AUTH", "Delete Token")
    }
}