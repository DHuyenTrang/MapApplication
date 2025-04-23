package com.example.mapapplication.utils

import android.util.Log
import vn.map4d.map.camera.MFCameraPosition
import vn.map4d.map.camera.MFCameraUpdateFactory
import vn.map4d.map.core.Map4D
import vn.map4d.types.MFLocationCoordinate
import kotlin.math.min

object Utils {

    fun calculateDistanceToRoad(
        currentLocation: MFLocationCoordinate,
        roadCoordinates: List<MFLocationCoordinate>
    ): Double? {
        var closestPoint: MFLocationCoordinate? = null
        if (roadCoordinates.isEmpty() || roadCoordinates.size < 2) return null
        var minDistance = Double.MAX_VALUE
        for (i in 0 until roadCoordinates.size - 1) {
            val start = roadCoordinates[i]
            val end = roadCoordinates[i + 1]
            closestPoint = getClosetCoordinate(currentLocation, start, end)
            val distance = currentLocation.distance(closestPoint)
            minDistance = min(minDistance, distance)
        }
        return minDistance
    }

    private fun getClosetCoordinate(
        current: MFLocationCoordinate,
        start: MFLocationCoordinate,
        end: MFLocationCoordinate
    ): MFLocationCoordinate {
        val x = current.longitude     // X: longitude
        val y = current.latitude      // Y: latitude
        val x1 = start.longitude
        val y1 = start.latitude
        val x2 = end.longitude
        val y2 = end.latitude

        val dx = x2 - x1
        val dy = y2 - y1

        if (dx == 0.0 && dy == 0.0) return start

        val t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy)
        val tClamped = t.coerceIn(0.0, 1.0)

        val closestX = x1 + tClamped * dx
        val closestY = y1 + tClamped * dy

        // Lưu ý: closestY là latitude, closestX là longitude
        return MFLocationCoordinate(closestY, closestX)
    }


    fun interpolatePolyline(coordinates: List<MFLocationCoordinate>): List<MFLocationCoordinate> {
        val result = mutableListOf<MFLocationCoordinate>()
        for (i in 0 until coordinates.size - 1) {
            val start = coordinates[i]
            val end = coordinates[i + 1]
            result.add(start)
            val distance = start.distance(end)
            if (distance > 10.0) {
                val steps = (distance / 10.0).toInt()
                for (j in 1 until steps) {
                    val fraction = j.toDouble() / steps
                    val lat = start.latitude + fraction * (end.latitude - start.latitude)
                    val lng = start.longitude + fraction * (end.longitude - start.longitude)
                    result.add(MFLocationCoordinate(lat, lng))
                }
            }
        }
        result.add(coordinates.last())
        Log.d("Route", "Interpolated ${coordinates.size} points to ${result.size} points")
        return result
    }

    fun moveCameraToLocation(map4d: Map4D, lat: Double, lon: Double, bearing: Double) {

        val cameraPosition = MFCameraPosition.Builder()
            .target(MFLocationCoordinate(lat, lon))
            .bearing(bearing)
            .zoom(15.0)
            .build()

        map4d.animateCamera(
            MFCameraUpdateFactory.newCameraPosition(cameraPosition),
        )
    }
}