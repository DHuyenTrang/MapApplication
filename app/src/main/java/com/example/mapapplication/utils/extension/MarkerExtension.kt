package com.example.mapapplication.utils.extension

import android.location.Location
import android.os.Looper
import com.google.android.gms.maps.model.Marker
import vn.map4d.map.annotations.MFMarker
import vn.map4d.types.MFLocationCoordinate

fun MFMarker.animateToPosition(newLocation: Location) {
    val marker = this
    val start = this.position
    val end = MFLocationCoordinate(newLocation.latitude, newLocation.longitude)

    val handler = android.os.Handler(Looper.getMainLooper())
    val startTime = System.currentTimeMillis()
    val duration = 1000L

    handler.post(object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val t = (elapsed / duration.toFloat()).coerceIn(0f, 1f)

            val lat = start.latitude + (end.latitude - start.latitude) * t
            val lng = start.longitude + (end.longitude - start.longitude) * t

            marker.position = MFLocationCoordinate(lat, lng)

            if (t < 1f) {
                handler.postDelayed(this, 16) // ~60fps
            }
        }
    })
}