package com.example.mapapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.map4d.map.camera.MFCameraPosition
import vn.map4d.map.camera.MFCameraUpdateFactory
import com.google.android.filament.gltfio.UbershaderProvider
import vn.map4d.map.core.Map4D
import vn.map4d.types.MFLocationCoordinate
import java.net.URL
import java.nio.Buffer
import kotlin.math.min

object Utils {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun renderGLTFToBitMap(
        url: String,
        width: Int,
        height: Int
        ): Bitmap? = withContext(Dispatchers.IO) {
        // init filament
        val engine = Engine.create()
        val scene = engine.createScene()
        val view = engine.createView()
        val renderer = engine.createRenderer()

        val materialProvider = UbershaderProvider(engine)
        val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())

        val gltfData = URL(url).readBytes()
        val resourceLoader = ResourceLoader(engine)

        val asset = assetLoader.createAsset(gltfData.toBuffer()) ?: return@withContext null
        val resourceUris = asset.resourceUris ?: emptyArray()
        for (uri in resourceUris) {
            try {
                val resourceUrl = if (uri.startsWith("http")) uri else URL(URL(url).protocol, URL(url).host, uri).toString()
                val resourceData = URL(resourceUrl).readBytes()
                resourceLoader.addResourceData(uri, resourceData.toBuffer())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resourceLoader.loadResources(asset)
        scene.addEntities(asset.entities)

        // Configure view with camera
        view.scene = scene
        view.viewport = Viewport(0, 0, width, height)
        val camera = engine.createCamera(EntityManager.get().create())
        view.camera = camera
        camera.setProjection(45.0, width.toDouble() / height, 0.1, 10.0, Camera.Fov.VERTICAL)
        camera.lookAt(1.0, 1.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0) // Adjusted for visibility

        val surfaceTexture = SurfaceTexture(false)
        surfaceTexture.setDefaultBufferSize(width, height)
        val surface = Surface(surfaceTexture)
        val swapChain = engine.createSwapChain(surface)

        // Render the scene
        if (renderer.beginFrame(swapChain, 0)) {
            renderer.render(view)
            renderer.endFrame()
        }

        // Capture the rendered frame to a Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        surfaceTexture.updateTexImage()

        // Clean up
        resourceLoader.destroy()
        assetLoader.destroyAsset(asset)
        materialProvider.destroyMaterials()
        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroy()
        materialProvider.destroy()

        bitmap
    }

    private fun ByteArray.toBuffer(): Buffer {
        return java.nio.ByteBuffer.allocateDirect(size).apply {
            put(this@toBuffer)
            flip()
        }
    }

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

    fun moveCameraToLocation(map4d: Map4D, lat: Double, lon: Double, zoom: Double, tilt: Double, bearing: Float) {

        val cameraPosition = MFCameraPosition.Builder()
            .target(MFLocationCoordinate(lat, lon))
            .bearing(bearing.toDouble())
            .zoom(zoom)
            .tilt(tilt)
            .build()

        map4d.animateCamera(
            MFCameraUpdateFactory.newCameraPosition(cameraPosition),
        )
    }
}