package com.example.mapapplication.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.model.LocationSearched
import com.example.mapapplication.repository.LocationSearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchLocationViewModel(
    private val locationSearchRepository: LocationSearchRepository
): ViewModel() {
    private var _locationSearched = MutableStateFlow<List<LocationSearched>>(emptyList())
    val locationSearched : StateFlow<List<LocationSearched>> = _locationSearched.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    val isLoading : StateFlow<Boolean> = _isLoading.asStateFlow()

    val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            query.debounce(2000)
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collectLatest {
                    Log.d("SEARCH", "query: $it")
                    search(it)
                }

        }
    }

    fun search(input: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = locationSearchRepository.searchLocation(input)
            if(response.isSuccessful) {
                _locationSearched.value = response.body()!!.predictions.map { it.mapToLocationSearched() }
                Log.d("SearchLocationViewModel", "search: ${_locationSearched.value}")
                _isLoading.value = false
            }
            else {
                Log.d("SearchLocationViewModel", "search: ${response.code()}")
            }
        }
    }
}