package com.example.mapapplication

import vn.map4d.map.camera.MFCameraUpdateFactory
import vn.map4d.map.core.MFCoordinateBounds
import vn.map4d.map.core.Map4D
import vn.map4d.types.MFLocationCoordinate

object Utils {

    fun moveCameraToLocation(map4d: Map4D, lat: Double, lon: Double) {
        map4d.moveCamera(
            MFCameraUpdateFactory.newCoordinateBounds(
                MFCoordinateBounds(
                    MFLocationCoordinate(lat, lon),
                    MFLocationCoordinate(
                        lat + 0.001,
                        lon + 0.001
                    )
                ),
                10
            )
        )
    }


}