package com.example.mapapplication.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrentLocationViewModel: ViewModel() {
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _currentSpeed = MutableStateFlow<Int>(0)
    val currentSpeed = _currentSpeed.asStateFlow()

    fun setCurrentSpeed(speed: Int) {
        _currentSpeed.value = speed
        Log.d("Location", "setCurrentSpeed: $speed")
    }

    fun setCurrentLocation(location: Location?) {
        _currentLocation.value = location
    }
}