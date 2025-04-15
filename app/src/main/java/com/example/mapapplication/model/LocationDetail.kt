package com.example.mapapplication.model

data class LocationDetail(
    val placeId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
) {
}