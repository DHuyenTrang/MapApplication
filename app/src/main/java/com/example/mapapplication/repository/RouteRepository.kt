package com.example.mapapplication.repository

import com.example.mapapplication.network.APIService

class RouteRepository(
    private val apiService: APIService
) {
    suspend fun searchRoute(bearings: Int, dstLat: Double, dstLng: Double, srcLat: Double, srcLng: Double) =
        apiService.searchRoute(bearings, dstLat, dstLng, srcLat, srcLng)
}