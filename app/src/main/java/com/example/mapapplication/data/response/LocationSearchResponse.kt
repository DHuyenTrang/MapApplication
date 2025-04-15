package com.example.mapapplication.data.response

import com.example.mapapplication.model.LocationSearched

data class LocationSearchResponse(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val structured_formatting: StructuredFormatting,
    val place_id: String,
) {
    fun mapToLocationSearched(): LocationSearched {
        val description = structured_formatting.main_text
        val address = structured_formatting.secondary_text
        return LocationSearched(place_id, description, address)
    }
}

data class StructuredFormatting(
    val main_text: String,
    val secondary_text: String
)
