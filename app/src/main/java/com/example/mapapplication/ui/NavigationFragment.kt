package com.example.mapapplication.ui

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var currentLocationMarker: MFMarker? = null

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

    }

    private fun addMarkerToMap(location: Location) {
        currentLocationMarker?.remove()

        currentLocationMarker = map4D.addMarker(
            MFMarkerOptions()
                .position(MFLocationCoordinate(location.latitude, location.longitude))
                .icon(MFBitmapDescriptorFactory.fromResource(R.drawable.ic_navigation))
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
        }
    }
}