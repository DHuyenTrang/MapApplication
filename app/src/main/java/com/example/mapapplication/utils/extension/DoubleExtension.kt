package com.example.mapapplication.utils.extension

import kotlin.math.roundToInt

fun Double.toDuration(): String {
    val hours = this / 3600
    if (hours >= 1) {
        return "${hours.toInt()} giờ ${((hours - hours.toInt()) * 60).toInt()} phút"
    }
    else {
        val minutes = this / 60
        return "${minutes.toInt()} phút"
    }
}

fun Double.toDistance(): String {
    val kilometers = this / 1000
    val distance = String.format("%.1f km", kilometers)
    return distance
}