package com.example.mapapplication.repository

import com.example.mapapplication.network.APIService

class LocationSearchRepository(
    private val apiService: APIService,
) {
    suspend fun searchLocation(input: String) = apiService.searchLocation(input)

    suspend fun getLocationDetail(placeId: String) = apiService.detailLocation(placeId)
}