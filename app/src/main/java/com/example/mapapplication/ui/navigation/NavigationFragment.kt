package com.example.mapapplication.ui.navigation

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mapapplication.R
import com.example.mapapplication.databinding.FragmentNavigationBinding
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
import vn.map4d.map.annotations.MFPolyline
import vn.map4d.map.annotations.MFPolylineOptions
import vn.map4d.map.camera.MFCameraPosition
import vn.map4d.map.camera.MFCameraUpdateFactory
import vn.map4d.map.core.MFMapType
import vn.map4d.map.core.Map4D
import vn.map4d.map.core.OnMapReadyCallback
import vn.map4d.types.MFLocationCoordinate

class NavigationFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map4D: Map4D

    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!

    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModel()
    private val routeViewModel: RouteViewModel by activityViewModel()

    private var currentPolyline: MFPolyline? = null
    private var currentLocationMarker: MFMarker? = null
    private var lastCameraPosition: MFLocationCoordinate? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setupUI()
    }

    private fun setupUI() {
        // close
        binding.btnClose.setOnClickListener {
            currentLocationMarker?.remove()
            currentPolyline?.remove()
            routeViewModel.setNavigationStepIndex(0)
            findNavController().navigateUp()
        }

        // instruction
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.navigationStepIndex.collectLatest {
                val step = routeViewModel.steps.value?.get(it)
                val instruction = step?.maneuver?.instruction
                binding.tvInstruction.text = instruction

            }
        }
    }

    private fun routing() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentLocationViewModel.currentLocation.collectLatest { location ->
                if (location != null) {
                    if (currentLocationMarker == null) addMarkerToMap(location)
//                    moveCameraToLocation(map4D, location.latitude, location.longitude, location.bearing.toDouble())
                    val current = MFLocationCoordinate(location.latitude, location.longitude)
                    val from = lastCameraPosition ?: current
                    animateCameraSmoothlyTo(map4D, from, current, location.bearing.toDouble())
                    lastCameraPosition = current
                    animateMarkerToPosition(currentLocationMarker!!, location)
                    routeViewModel.calculateDistanceRemaining(location)
                }
            }
        }
    }

    private fun animateCameraSmoothlyTo(
        map4D: Map4D,
        from: MFLocationCoordinate,
        to: MFLocationCoordinate,
        newBearing: Double) {
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

                val currentBearing = map4D.cameraPosition?.bearing ?: 0.0
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

    private fun animateMarkerToPosition(marker: MFMarker, newLocation: Location) {
        val start = marker.position
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

    private fun addMarkerToMap(location: Location) {
        currentLocationMarker?.remove()

        currentLocationMarker = map4D.addMarker(
            MFMarkerOptions()
                .position(MFLocationCoordinate(location.latitude, location.longitude))
                .icon(MFBitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                .zIndex(15f)
        )
    }

    private fun drawRoute() {
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.coordinates.collect { coordinates ->
                getRouteToDraw(coordinates, Color.BLUE)
            }
        }
    }

    private fun getRouteToDraw(coordinates: List<MFLocationCoordinate>, color: Int) {
        if (coordinates.isNotEmpty()) {
            currentPolyline?.remove()
            currentPolyline = map4D.addPolyline(
                MFPolylineOptions().add(*coordinates.toTypedArray())
                    .color(color)
                    .width(20.0f)
                    .zIndex(10f)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            routing()
            drawRoute()
            map4D.mapType = MFMapType.ROADMAP
        }
    }
}