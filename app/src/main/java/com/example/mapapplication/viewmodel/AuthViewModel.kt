package com.example.mapapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.TokenManager
import com.example.mapapplication.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    private val _isLoginSuccessful = MutableStateFlow<Boolean?>(null)
    val isLoginSuccessful: StateFlow<Boolean?> = _isLoginSuccessful.asStateFlow()

    private var _isLogout = MutableStateFlow<Boolean?>(null)
    val isLogout: StateFlow<Boolean?> = _isLogout.asStateFlow()

    fun fetchUser(phoneNumber: String, password: String) {
        viewModelScope.launch {
            val response = authRepository.login(phoneNumber, password)

            if (response.isSuccessful) {
                response.body()?.let {
                    tokenManager.saveToken(it.access_token, it.refresh_token)
                    tokenManager.saveUserId(it.customer_id)
                    _isLoginSuccessful.value = true
                    //Log.d("Auth", "Login Success: ${it.access_token}")
                }
            } else {
                Log.e("Auth", "Login Failed: ${response.errorBody()?.string()}")
                _isLoginSuccessful.value = false
            }
        }
    }

    fun logout(){
        tokenManager.clearToken()
        _isLogout.value = true
    }
}