package com.example.mapapplication.manager

import kotlinx.coroutines.flow.MutableStateFlow

class SessionManager(
    private val tokenManager: TokenManager
) {
    private val _isLogout = MutableStateFlow<Boolean>(false)

}