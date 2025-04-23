package com.example.mapapplication.utils.extension

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Looper
import com.example.mapapplication.R
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
import vn.map4d.map.annotations.MFPolyline
import vn.map4d.map.annotations.MFPolylineOptions
import vn.map4d.map.camera.MFCameraPosition
import vn.map4d.map.camera.MFCameraUpdateFactory
import vn.map4d.map.core.MFCoordinateBounds
import vn.map4d.map.core.Map4D
import vn.map4d.types.MFLocationCoordinate

fun Map4D.drawMarker(oldMarker: MFMarker?, lat: Double, lon: Double, source: Int): MFMarker {
    oldMarker?.remove()
    val newMarker = this.addMarker(
        MFMarkerOptions()
            .position(MFLocationCoordinate(lat, lon))
            .icon(MFBitmapDescriptorFactory.fromResource(source))
    )
    return newMarker
}

fun Map4D.moveCameraToLocation(lat: Double, lon: Double, bearing: Double, paddingBottom: Int) {
    val locationCoordinate = MFLocationCoordinate(lat, lon)
    val bounds = MFCoordinateBounds(locationCoordinate, locationCoordinate)
    val cameraLocation = MFCameraUpdateFactory
        .newCoordinateBounds(
            bounds,
            0, 0, 0, paddingBottom
        )
    this.animateCamera(
        cameraLocation
    )
}

fun Map4D.drawRoute(currentPolyline: MFPolyline?, coordinates: List<MFLocationCoordinate>): MFPolyline? {
    if (coordinates.isNotEmpty()) {
        currentPolyline?.remove()
        this.addPolyline(
            MFPolylineOptions().add(*coordinates.toTypedArray())
                .color(Color.parseColor("#FF629BF8"))
                .width(10.0f)
                .zIndex(11f)
        )

        return this.addPolyline(
            MFPolylineOptions().add(*coordinates.toTypedArray())
                .color(Color.parseColor("#FF183668"))
                .width(14.0f)
                .zIndex(10f)
        )
    }
    else
        return null
}

fun Map4D.animateCameraSmoothlyTo(
    from: MFLocationCoordinate,
    to: MFLocationCoordinate,
    newBearing: Double
) {
    val map4D = this
    val handler = android.os.Handler(Looper.getMainLooper())
    val startTime = System.currentTimeMillis()
    val duration = 1000L

    handler.post(object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val t = (elapsed / duration.toFloat()).coerceIn(0f, 1f)

            val lat = from.latitude + (to.latitude - from.latitude) * t
            val lng = from.longitude + (to.longitude - from.longitude) * t

            val interpolated = MFLocationCoordinate(lat, lng)

            val currentBearing = map4D.cameraPosition.bearing ?: 0.0
            val bearing = interpolateBearing(currentBearing, newBearing, t)

            val cameraPosition = MFCameraPosition.Builder()
                .target(interpolated)
                .zoom(20.0)
                .bearing(bearing)
                .tilt(45.0)
                .build()

            map4D.moveCamera(MFCameraUpdateFactory.newCameraPosition(cameraPosition))

            if (t < 1f) {
                handler.postDelayed(this, 16) // ~60 FPS
            }
        }
    })
}

private fun interpolateBearing(from: Double, to: Double, t: Float): Double {
    val delta = ((to - from + 540) % 360) - 180
    return (from + delta * t + 360) % 360
}