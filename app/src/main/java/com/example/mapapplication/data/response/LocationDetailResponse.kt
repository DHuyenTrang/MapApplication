package com.example.mapapplication.data.response

import com.example.mapapplication.model.LocationDetail

data class LocationDetailResponse(
    val result: LocationDetailResult,
)

data class LocationDetailResult (
    val place_id: String,
    val geometry: LocationDetailGeometry,
    val formatted_address: String,
    val name: String,
) {
    fun mapToLocationDetail() = LocationDetail(
        placeId = place_id,
        name = name,
        address = formatted_address,
        lat = geometry.location.lat,
        lng = geometry.location.lng,
    )
}

data class LocationDetailGeometry (
    val location: LocationDetailLocation
)

data class LocationDetailLocation(
    val lat: Double,
    val lng: Double
)