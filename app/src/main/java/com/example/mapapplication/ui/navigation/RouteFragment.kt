package com.example.mapapplication.ui.navigation

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mapapplication.R
import com.example.mapapplication.databinding.FragmentRouteBinding
import com.example.mapapplication.utils.extension.drawMarker
import com.example.mapapplication.viewmodel.RouteViewModel
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

class RouteFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    private val routeViewModel: RouteViewModel by activityViewModel()

    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private var destinationLat: Double? = null
    private var destinationLng: Double? = null

    private lateinit var map4D: Map4D
    private var currentLocationMarker: MFMarker? = null
    private var destinationMarker: MFMarker? = null
    private var currentPolyline: MFPolyline? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRouteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnStartNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_routeFragment_to_navigationFragment)
        }

        currentLat = arguments?.getDouble("currentLat")
        currentLng = arguments?.getDouble("currentLng")
        destinationLat = arguments?.getDouble("destinationLat")
        destinationLng = arguments?.getDouble("destinationLng")
        routeViewModel.searchRoute(180, destinationLat!!, destinationLng!!, currentLat!!, currentLng!!)
        Log.d("RouteFragment", "currentLat: $currentLat, currentLng: $currentLng, destinationLat: $destinationLat, destinationLng: $destinationLng")
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.coordinates.collect { coordinates ->
                getRouteToDraw(coordinates)
            }
        }
    }

    private fun getRouteToDraw(coordinates: List<MFLocationCoordinate>) {
        if (coordinates.isNotEmpty()) {
            currentPolyline?.remove()
            currentPolyline = map4D.addPolyline(
                MFPolylineOptions().add(*coordinates.toTypedArray())
                    .color(Color.BLUE)
                    .width(6.0f)
                    .zIndex(10f)
            )
            val builder = MFCoordinateBounds.Builder()
            coordinates.forEach { builder.include(it) }
            val bounds = builder.build()
            map4D.animateCamera(MFCameraUpdateFactory.newCoordinateBounds(bounds, 100))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
            currentLocationMarker = map4D.drawMarker(currentLocationMarker, currentLat!!, currentLng!!, R.drawable.ic_location)
            destinationMarker = map4D.drawMarker(destinationMarker, destinationLat!!, destinationLng!!, R.drawable.ic_pin_marker)
            observe()
        }
    }
}