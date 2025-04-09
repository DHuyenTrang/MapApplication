package com.example.mapapplication.data.response

data class RouteResponse(
    val routes: List<Route>
) {}
data class Route(
    val legs: List<Leg>
) {}
 data class Leg(
     val steps: List<Step>
 ) {}
 data class Step(
     val maneuver: Maneuver,
     val intersections: List<Intersection>,
     val geometry: String,
     val distance: Double,
     val duration: Double,
 ) {}

data class Intersection(
    val location: List<Double>
) {}

data class Maneuver(
    val location: List<Double>,
    val instruction: String,
)
