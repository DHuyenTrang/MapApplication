package com.example.mapapplication.ui.map

import android.util.Log
import vn.map4d.map.annotations.DirectionsRendererDelegate
import vn.map4d.map.core.MFMapView

class MapRendererDelegate(private val mapView: MFMapView) : DirectionsRendererDelegate {

    override fun updateDirectionsRenderer(id: Long) {
        // Có thể dùng để trigger cập nhật lại renderer nếu cần
        Log.d("MapRenderer", "updateDirectionsRenderer called with id: $id")
    }

    override fun setWidth(id: Long, v: Float) {
        Log.d("MapRenderer", "Set width for id: $id to $v")
        // Gọi vào MFDirectionsRenderer nào đó để set lại width nếu cần
    }

    override fun setOutlineWidth(id: Long, v: Float) {
        Log.d("MapRenderer", "Set outline width for id: $id to $v")
    }

    override fun remove(id: Long) {
        Log.d("MapRenderer", "Remove renderer with id: $id")
        // Nếu bạn đang giữ danh sách renderer, bạn cần xoá nó ra khỏi map hoặc xoá khỏi list
    }
}
