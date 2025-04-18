package com.example.mapapplication.utils.extension

import android.annotation.SuppressLint
import android.graphics.Color
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