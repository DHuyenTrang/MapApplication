package com.example.mapapplication.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.model.LocationDetail
import com.example.mapapplication.repository.LocationSearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Thread.State

class LocationDetailViewModel(
    private val locationSearchRepository: LocationSearchRepository,
) : ViewModel(){
    private var _locationDetail = MutableStateFlow<LocationDetail?>(null)
    val locationDetail : StateFlow<LocationDetail?> = _locationDetail.asStateFlow()

    fun fetchLocationDetail(placeId: String) {
        viewModelScope.launch {
            val response = locationSearchRepository.getLocationDetail(placeId)
            if (response.isSuccessful) {
                _locationDetail.value = response.body()?.result?.mapToLocationDetail()
                Log.d("LocationDetailViewModel", "fetchLocationDetail: ${_locationDetail.value}")
            }
        }
    }
}