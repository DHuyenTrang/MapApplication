package com.example.mapapplication.data.response

import com.example.mapapplication.model.PathInfor

data class RouteResponse(
    val routes: List<Route>
) {}
data class Route(
    val legs: List<Leg>,
    val distance: Double,
    val duration: Double,
) {
    fun mapToPathInfo(): PathInfor {
        return PathInfor(0, distance, duration)
    }
}
 data class Leg(
     val steps: List<Step>
 ) {}
 data class Step(
     val maneuver: Maneuver,
     val intersections: List<Intersection>,
     val geometry: String,
     val distance: Double,
     val duration: Double,
     val name: String,
 ) {}

data class Intersection(
    val location: List<Double>
) {}

data class Maneuver(
    val location: List<Double>,
    val instruction: String,
    val type: String,
    val modifier: String
)
