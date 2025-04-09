package com.example.mapapplication.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.mapapplication.R
import com.example.mapapplication.Utils.moveCameraToLocation
import com.example.mapapplication.databinding.FragmentNavigationBinding
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
import vn.map4d.map.annotations.MFPolyline
import vn.map4d.map.annotations.MFPolylineOptions
import vn.map4d.map.camera.MFCameraUpdateFactory
import vn.map4d.map.core.MFCoordinateBounds
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: MFMarker? = null
    private val locationRequest = LocationRequest.create().apply {
        interval = 3000 // 3 giây
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        observeSteps()
        observeDistanceRemaining()
        observeCurrentLocation()
    }

    private fun observeSteps() {
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.steps.collect { steps ->
                if (steps != null) {
                    binding.tvInstruction.text =
                        steps[routeViewModel.navigationStepIndex.value].maneuver.instruction
                }
            }
        }
    }

    private fun observeDistanceRemaining() {
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.distanceRemaining.collect { distance ->
                Log.d("Route", "Distance remaining: $distance")
                if (distance == 0.0) routeViewModel.updateNavigationStepIndex()
            }
        }
    }

    private fun observeCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentLocationViewModel.currentLocation.collect { location ->
                if (location != null) {
                    updateCurrentLocationOnMap(location)
                }
            }
        }
    }

    private fun updateCurrentLocationOnMap(location: Location) {
        val latLng = MFLocationCoordinate(location.latitude, location.longitude)
        if (currentLocationMarker == null) {
            addMarkerToMap(location)

        } else {
            currentLocationMarker?.position = latLng
        }

        moveCameraToLocation(map4D, location.latitude, location.longitude)
        routeViewModel.calculateDistanceRemaining(latLng)
        // Nếu bạn muốn vẽ đường đã đi (polyline), thêm vào list và vẽ lại
//        pathPoints.add(latLng)
//        drawPathOnMap()
    }

    private fun addMarkerToMap(location: Location) {
        currentLocationMarker?.remove()

        currentLocationMarker = map4D.addMarker(
            MFMarkerOptions()
                .position(MFLocationCoordinate(location.latitude, location.longitude))
                .icon(MFBitmapDescriptorFactory.fromResource(R.drawable.ic_navigation))
        )
    }

    private fun drawRoute() {
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.coordinates.collect { coordinates ->
                if (coordinates.isNotEmpty()) {
                    currentPolyline?.remove()
                    currentPolyline = map4D.addPolyline(
                        MFPolylineOptions().add(*coordinates.toTypedArray())
                            .color(Color.BLUE)
                            .width(6.0f)
                            .zIndex(10f)
                    )
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
            drawRoute()
        }
    }
}