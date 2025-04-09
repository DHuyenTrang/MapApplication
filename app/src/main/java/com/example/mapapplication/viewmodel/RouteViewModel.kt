package com.example.mapapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.TokenManager
import com.example.mapapplication.data.response.Step
import com.example.mapapplication.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.map4d.types.MFLocationCoordinate

class RouteViewModel(
    private val routeRepository: RouteRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _coordinates = MutableStateFlow<List<MFLocationCoordinate>>(emptyList())
    val coordinates: StateFlow<List<MFLocationCoordinate>> = _coordinates.asStateFlow()

    private var _isLoading = MutableStateFlow<Boolean?>(null)
    val isLoading: StateFlow<Boolean?> = _isLoading.asStateFlow()

    private var _steps = MutableStateFlow<List<Step>?>(null)
    val steps: StateFlow<List<Step>?> = _steps.asStateFlow()

    private var _isLogout = MutableStateFlow<Boolean?>(null)
    val isLogout: StateFlow<Boolean?> = _isLogout.asStateFlow()

    private var _navigationStepIndex = MutableStateFlow<Int>(0)
    val navigationStepIndex: StateFlow<Int> = _navigationStepIndex.asStateFlow()

    private var _distanceRemaining = MutableStateFlow<Double?>(null)
    val distanceRemaining: StateFlow<Double?> = _distanceRemaining.asStateFlow()

    fun calculateDistanceRemaining(currentLocation: MFLocationCoordinate) {
        val currentStep = _steps.value?.get(_navigationStepIndex.value)
        val nextPoint = currentStep?.maneuver?.location?.let { MFLocationCoordinate(it[1], it[0]) }
        Log.d("RouteViewModel", "Navigation step index: ${_navigationStepIndex.value}")
        Log.d("RouteViewModel", "Next point: ${nextPoint?.latitude} ${nextPoint?.longitude}")
        val distance = currentLocation.distance(nextPoint!!)
        Log.d("RouteViewModel", "Distance: $distance")
        _distanceRemaining.value = distance
    }

    fun updateNavigationStepIndex() {
            if (_navigationStepIndex.value < (_steps.value?.size ?: 0) - 1) {
                _navigationStepIndex.value++
            }
    }

    fun searchRoute(bearings: Int, dstLat: Double, dstLng: Double, srcLat: Double, srcLng: Double) {
        Log.d("RouteViewModel", "in searchRoute")
        _isLoading.value = true
        Log.d("Route", "Is loading: ${_isLoading.value}")
        viewModelScope.launch {
            val response = routeRepository.searchRoute(bearings, dstLat, dstLng, srcLat, srcLng)
            if (response.isSuccessful) {
                // first route
                response.body()?.let {
                    val steps = it.routes
                        .firstOrNull()
                        ?.legs?.firstOrNull()
                        ?.steps
                    // emit coordinates to draw route
                    _coordinates.value = getCoordinatesFromStep(steps)

                    // emit steps to route turn by turn
                    _steps.value = steps
                    for (step in steps ?: emptyList()) {
                        Log.d("RouteViewModel", "Step: ${step.maneuver.instruction}")
                    }
                    Log.d("RouteViewModel", "1stCoordinates: ${response.body()}")
                }
                _isLoading.value = false
                Log.d("Route", "Is loading: ${_isLoading.value}")
            } else {
                Log.e("RouteViewModel", "searchRoute failed: ${response.errorBody()?.string()}")
                if (response.code() == 644) {
                    logout()
                }
            }
        }
    }

    private fun getCoordinatesFromStep(steps: List<Step>?): List<MFLocationCoordinate> {
        val listCoordinate = mutableListOf<MFLocationCoordinate>()
        for (step: Step in steps ?: emptyList()) {
            val location = step.intersections.firstOrNull()?.location
            val geometry = step.geometry
            listCoordinate.addAll(decodePolyline(geometry))
        }
        return listCoordinate
    }

    private fun decodePolyline(encoded: String): List<MFLocationCoordinate> {
        val polyline = mutableListOf<MFLocationCoordinate>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var result = 0
            var shift = 0
            var b: Int

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            result = 0
            shift = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val latLng = MFLocationCoordinate(lat / 1e5, lng / 1e5)
            polyline.add(latLng)
        }

        return polyline
    }
    private fun logout(){
        tokenManager.clearToken()
        _isLogout.value = true
    }
}