package com.example.mapapplication.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.manager.TokenManager
import com.example.mapapplication.data.response.Step
import com.example.mapapplication.model.PathInfor
import com.example.mapapplication.repository.RouteRepository
import com.example.mapapplication.utils.Constant
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

    private val _isLoading = MutableStateFlow<Boolean?>(null)
    val isLoading: StateFlow<Boolean?> = _isLoading.asStateFlow()

    private val _steps = MutableStateFlow<List<Step>?>(null)
    val steps: StateFlow<List<Step>?> = _steps.asStateFlow()

    private val _navigationStepIndex = MutableStateFlow<Int>(0)
    val navigationStepIndex: StateFlow<Int> = _navigationStepIndex.asStateFlow()

    private val _distanceRemaining = MutableStateFlow<Double?>(null)
    val distanceRemaining: StateFlow<Double?> = _distanceRemaining.asStateFlow()

    private val _pathInfor = MutableStateFlow<PathInfor?>(null)
    val pathInfor: StateFlow<PathInfor?> = _pathInfor.asStateFlow()

    private val _locationStep = MutableStateFlow<String?>(null)
    val locationStep: StateFlow<String?> = _locationStep.asStateFlow()

    private val _typeSign = MutableStateFlow<Int?>(null)
    val typeSign: StateFlow<Int?> = _typeSign.asStateFlow()

    fun setNavigationStepIndex(index: Int) {
        _navigationStepIndex.value = index
    }
    fun calculateDistanceRemaining(location: Location) {
        val currentLocation = MFLocationCoordinate(location.latitude, location.longitude)
        val currentStep = _steps.value?.get(_navigationStepIndex.value)
        currentStep?.maneuver?.instruction?.let { getTypeSign(it) }

        val nextPoint = currentStep?.maneuver?.location?.let { MFLocationCoordinate(it[1], it[0]) }
        val distance = currentLocation.distance(nextPoint!!)
        Log.d("RouteViewModel", "Distance: $distance")

        _distanceRemaining.value = distance
        if (distance <= 10) {
            updateNavigationStepIndex()
        }
    }

    private fun updateNavigationStepIndex() {
            if (_navigationStepIndex.value < (_steps.value?.size ?: 0) - 1) {
                _navigationStepIndex.value++
            }
    }

    fun searchRoute(bearings: Int, dstLat: Double, dstLng: Double, srcLat: Double, srcLng: Double) {
        if (_isLoading.value == true) return
        Log.d("Route", "in searchRoute")
        _isLoading.value = true
        Log.d("Route", "Is loading: ${_isLoading.value}")
        viewModelScope.launch {
            val response = routeRepository.searchRoute(bearings, dstLat, dstLng, srcLat, srcLng)
            if (response.isSuccessful) {
                // first route
                response.body()?.let {

                    // emit coordinates to draw route
                    val steps = it.routes.firstOrNull()?.legs?.firstOrNull()?.steps
                    _coordinates.value = getCoordinatesFromStep(steps)

                    // emit steps to route turn by turn
                    _steps.value = steps
                    for (step in steps ?: emptyList()) {
                        Log.d("Route", "Step: ${step.maneuver.instruction}")
                    }

                    // emit location, duration
                    _pathInfor.value = it.routes.firstOrNull()?.mapToPathInfo()
                }
                _isLoading.value = false
                Log.d("Route", "Is loading: ${_isLoading.value}")
            } else {
                Log.e("Route", "searchRoute failed: ${response.errorBody()?.string()}")

            }
        }
    }

    private fun getTypeSign(instruction: String) {
        if (instruction.contains("U-turn")) {
            if (instruction.contains("left")) {
                _typeSign.value = Constant.TYPE_SIGN_U_LEFT
            } else {
                _typeSign.value = Constant.TYPE_SIGN_U_RIGHT
            }
        }
        else {
            if (instruction.contains("left")) {
                _typeSign.value = Constant.TYPE_SIGN_LEFT
            } else if (instruction.contains("right")) {
                _typeSign.value = Constant.TYPE_SIGN_RIGHT
            }
            else {
                _typeSign.value = null
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
}