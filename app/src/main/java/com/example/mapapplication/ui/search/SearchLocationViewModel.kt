package com.example.mapapplication.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.model.LocationSearched
import com.example.mapapplication.repository.LocationSearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchLocationViewModel(
    private val locationSearchRepository: LocationSearchRepository
): ViewModel() {
    private var _locationSearched = MutableStateFlow<List<LocationSearched>>(emptyList())
    val locationSearched : StateFlow<List<LocationSearched>> = _locationSearched.asStateFlow()

    fun search(input: String) {
        viewModelScope.launch {
            val response = locationSearchRepository.searchLocation(input)
            if(response.isSuccessful) {
                _locationSearched.value = response.body()!!.predictions.map { it.mapToLocationSearched() }
                Log.d("SearchLocationViewModel", "search: ${_locationSearched.value}")
            }
            else {
                Log.d("SearchLocationViewModel", "search: ${response.code()}")
            }
        }
    }
}