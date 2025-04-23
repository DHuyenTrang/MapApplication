package com.example.mapapplication.ui.navigation

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mapapplication.R
import com.example.mapapplication.databinding.FragmentNavigationBinding
import com.example.mapapplication.ui.map.MapRendererDelegate
import com.example.mapapplication.utils.extension.animateCameraSmoothlyTo
import com.example.mapapplication.utils.extension.animateToPosition
import com.example.mapapplication.utils.extension.drawRoute
import com.example.mapapplication.utils.extension.toDistance
import com.example.mapapplication.utils.extension.toDuration
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import vn.map4d.map.annotations.DirectionsRendererDelegate
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFDirectionsRenderer
import vn.map4d.map.annotations.MFDirectionsRendererOptions
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
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date

class NavigationFragment : Fragment(), OnMapReadyCallback{
    private lateinit var map4D: Map4D

    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!

    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModel()
    private val routeViewModel: RouteViewModel by activityViewModel()

    private var destinationLat: Double? = null
    private var destinationLng: Double? = null

    private var currentDirection : MFDirectionsRenderer? = null
    private var destinationMarker: MFMarker? = null
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

        destinationLat = arguments?.getDouble("destinationLat")
        destinationLng = arguments?.getDouble("destinationLng")

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setupUI()
    }

    private fun setupUI() {
        val bottomSheet = binding.bottomSheetDashboard.root
        val bottomSheetBehavior =
            BottomSheetBehavior.from<LinearLayout>(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        // close
        binding.bottomSheetDashboard.btnClose.setOnClickListener {
            currentLocationMarker?.remove()
            currentDirection?.remove()
            routeViewModel.setNavigationStepIndex(0)
            findNavController().navigateUp()
        }
        // distance remaining
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.distanceRemaining.collectLatest { distance ->
                if (distance != null) {
                    binding.bottomSheetDashboard.tvDistanceStepRemaining.text =
                        distance.toDistance()
                }
            }
        }
        // sign
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.typeSign.collectLatest { sign ->
                if (sign != null) {
                    binding.bottomSheetDashboard.icNavigation.setImageResource(sign)
                }
            }
        }
        // currentTime
        val currentTime = SimpleDateFormat("HH:mm").format(Date())
        binding.bottomSheetDashboard.tvCurrentTime.text = currentTime
        // duration and place
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.pathInfor.collect { pathInfor ->
                pathInfor?.let {
                    binding.bottomSheetDashboard.tvDistance.text = it.distance.toDistance()
                    binding.bottomSheetDashboard.tvDuration.text = it.duration.toDuration()
                }
            }
        }
        // is deviated
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.isDeviated.collectLatest { isDeviated ->
                if (isDeviated) {
                    routeViewModel.searchRoute(
                        currentLocationViewModel.currentLocation.value!!.bearing.toInt(),
                        destinationLat!!,
                        destinationLng!!,
                        currentLocationViewModel.currentLocation.value!!.latitude,
                        currentLocationViewModel.currentLocation.value!!.longitude
                    )
                    routeViewModel.setNavigationStepIndex(0)
                }
            }
        }
    }

    private fun routing() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentLocationViewModel.currentLocation.collectLatest { location ->
                if (location != null) {
                    if (currentLocationMarker == null) addMarkerToMap(location)
                    val current = MFLocationCoordinate(location.latitude, location.longitude)

                    val from = lastCameraPosition ?: current
                    map4D.animateCameraSmoothlyTo(from, current, location.bearing.toDouble())
                    lastCameraPosition = current

                    currentLocationMarker?.animateToPosition(location)
                    routeViewModel.calculateDistanceRemaining(location)
                    routeViewModel.checkRouteDeviation(location)
                }
            }
        }
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
            routeViewModel.coordinates.collectLatest { coordinates ->
                if (coordinates.isNotEmpty()) {
                    currentDirection?.remove()
                    val path = coordinates.toList()
                    val paths: List<List<MFLocationCoordinate>> = arrayListOf(path)

                    val options = MFDirectionsRendererOptions()
                        .paths(paths)
                        .activeStrokeColor(Color.parseColor("#FF629BF8"))
                        .inactiveStrokeColor(Color.parseColor("#FF183668"))
                        .activeOutlineColor(Color.parseColor("#FF183668"))
                        .width(10.0f)
                        .outlineWidth(2f)
                    currentDirection = map4D.addDirectionsRenderer(options)
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
            map4D.mapType = MFMapType.MAP3D
            routing()
            drawRoute()

            destinationMarker = map4D.addMarker(
                MFMarkerOptions()
                    .position(MFLocationCoordinate(destinationLat!!, destinationLng!!))
                    .icon(MFBitmapDescriptorFactory.fromResource(R.drawable.ic_pin_marker))
                    .zIndex(15f)
            )
        }
    }
}