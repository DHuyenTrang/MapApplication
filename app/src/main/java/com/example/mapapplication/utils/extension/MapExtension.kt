package com.example.mapapplication.utils.extension

import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
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