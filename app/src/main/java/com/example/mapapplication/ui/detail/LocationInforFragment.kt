package com.example.mapapplication.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mapapplication.R
import com.example.mapapplication.Utils
import com.example.mapapplication.databinding.FragmentLocationInforBinding
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import vn.map4d.map.core.MFMapType
import vn.map4d.map.core.Map4D
import vn.map4d.map.core.OnMapReadyCallback
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
import vn.map4d.map.annotations.MFPolyline
import vn.map4d.map.annotations.MFPolylineOptions
import vn.map4d.types.MFLocationCoordinate

class LocationInforFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentLocationInforBinding? = null
    private val binding get() = _binding!!

    private val locationDetailViewModel: LocationDetailViewModel by viewModel()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModel()
    private val routeViewModel: RouteViewModel by activityViewModel()

    private var placeId: String = ""
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
        _binding = FragmentLocationInforBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        placeId = arguments?.getString("place_id") ?: ""
        Log.d("SearchLocation", "placeId: $placeId")

        locationDetailViewModel.fetchLocationDetail(placeId)
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            locationDetailViewModel.locationDetail.collectLatest {
                if (it != null) {
                    destinationLat = it.lat
                    destinationLng = it.lng
                    Utils.moveCameraToLocation(map4D, it.lat, it.lng, 0.0)
                    drawMarker(destinationMarker, it.lat, it.lng, R.drawable.ic_marker_destination)
                    binding.tvNameLocation.text = it.name
                    binding.tvAddressLocation.text = it.address

                    trySearchRoute()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            currentLocationViewModel.currentLocation.collectLatest {
                if (it != null) {
                    currentLat = it.latitude
                    currentLng = it.longitude
                    drawMarker(currentLocationMarker, it.latitude, it.longitude, R.drawable.ic_location)

                    trySearchRoute()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            routeViewModel.coordinates.collect { coordinates ->
                getRouteToDraw(coordinates)
            }
        }
    }

    private fun trySearchRoute() {
        if (currentLat != null && currentLng != null && destinationLat != null && destinationLng != null) {
            routeViewModel.searchRoute(180,
                destinationLat!!, destinationLng!!, currentLat!!, currentLng!!)
        }
    }

    private fun drawMarker(marker: MFMarker?, lat: Double, lon: Double, source: Int) {
        marker?.remove()
        val newMarker = map4D.addMarker(
            MFMarkerOptions()
                .position(MFLocationCoordinate(lat, lon))
                .icon(MFBitmapDescriptorFactory.fromResource(source))
        )

        // Gán lại marker tương ứng
        when (source) {
            R.drawable.ic_location -> currentLocationMarker = newMarker
            R.drawable.ic_marker_destination -> destinationMarker = newMarker
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
        }
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
            observe()
        }
    }

}