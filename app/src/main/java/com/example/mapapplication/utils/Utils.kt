package com.example.mapapplication.utils

import vn.map4d.map.camera.MFCameraPosition
import vn.map4d.map.camera.MFCameraUpdateFactory
import vn.map4d.map.core.Map4D
import vn.map4d.types.MFLocationCoordinate

object Utils {

    fun moveCameraToLocation(map4d: Map4D, lat: Double, lon: Double, bearing: Double) {

        val cameraPosition = MFCameraPosition.Builder()
            .target(MFLocationCoordinate(lat, lon))
            .bearing(bearing)
            .build()

        map4d.animateCamera(
            MFCameraUpdateFactory.newCameraPosition(cameraPosition),
        )
    }
}