package com.example.mapapplication.ui

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mapapplication.R
import com.example.mapapplication.Utils.moveCameraToLocation
import com.example.mapapplication.databinding.FragmentMapBinding
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
import vn.map4d.map.core.MFMapType
import vn.map4d.map.core.Map4D
import vn.map4d.map.core.OnMapReadyCallback
import vn.map4d.types.MFLocationCoordinate

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModel()

    private lateinit var map4D: Map4D
    private var currentLocationMarker: MFMarker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        listenEvent()
//        setUpBottomSheet()
    }

//    private fun setUpBottomSheet() {
//        val bottomSheet = binding.bottomSheet.bottomSheet
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
//        binding.bottomSheet.btnStartNavigation.setOnClickListener {
//            findNavController().navigate(R.id.action_mapFragment_to_navigationFragment)
//        }
//    }

//    private fun drawRoute() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            routeViewModel.coordinates.collect { coordinates ->
//                getRouteToDraw(coordinates, Color.BLUE)
//            }
//        }
//    }

//    private fun getRouteToDraw(coordinates: List<MFLocationCoordinate>, color: Int) {
//        if (coordinates.isNotEmpty()) {
//            currentPolyline?.remove()
//            currentPolyline = map4D.addPolyline(
//                MFPolylineOptions().add(*coordinates.toTypedArray())
//                    .color(color)
//                    .width(6.0f)
//                    .zIndex(10f)
//            )
//        }
//    }

    private fun listenEvent() {
        binding.btnMyLocation.setOnClickListener {
            setUpCurrentLocation()
        }
        binding.edtSearch.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_searchLocationFragment)
        }
    }

    private fun setUpCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            val location = currentLocationViewModel.currentLocation.value
            if (location != null) {
                Log.d("Location", "current location: ${location.latitude} ${location.longitude}")
                if (currentLocationMarker == null) addMarkerToMap(location)
                else{
                    currentLocationMarker!!.remove()
                    addMarkerToMap(location)
                }
                moveCameraToLocation(map4D, location.latitude, location.longitude, 0.0)
            } else {
                moveCameraToLocation(map4D, 20.98085354867591, 105.78798040202281, 0.0)
            }
        }
    }

    private fun addMarkerToMap(location: Location) {
        currentLocationMarker = map4D.addMarker(
            MFMarkerOptions()
                .position(MFLocationCoordinate(location.latitude, location.longitude))
                .icon(MFBitmapDescriptorFactory.fromResource(R.drawable.ic_location))
        )
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
            setUpCurrentLocation()
//            map4D.setOnMapClickListener {
//                Log.d("MapFragment", "Map clicked at: ${it.latitude}, ${it.longitude}")
//                createDialog(MFLocationCoordinate(20.98085354867591, 105.78798040202281))
//            }
        }

    }
//    private fun createDialog(coordinate: MFLocationCoordinate) {
//        val builder = AlertDialog.Builder(requireContext())
//        builder
//            .setTitle("Thông báo")
//            .setMessage("Chỉ đường đến vị trí: ${coordinate.latitude} ${coordinate.longitude}")
//            .setPositiveButton("OK") { dialog, which ->
//
//                routeViewModel.searchRoute(
//                    180,
//                    coordinate.latitude,
//                    coordinate.longitude,
//                    currentLocationViewModel.currentLocation.value!!.latitude,
//                    currentLocationViewModel.currentLocation.value!!.longitude
//                )
//                drawRoute()
//            }
//            .setNegativeButton("Cancel") { dialog, which ->
//                dialog.dismiss()
//            }
//        builder.create().show()
//    }

//    private fun observeLoading() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            routeViewModel.isLoading.collect { isLoading ->
//                if (isLoading != null) {
//                    if(isLoading) {
//                        Log.d("Route", "ProgressBar is visible")
//                        binding.progressBar.visibility = View.VISIBLE
//                    }
//                    else{
//                        binding.progressBar.visibility = View.GONE
//                        binding.btnRoute.visibility = View.VISIBLE
//                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                    }
//                }
//            }
//        }
//    }
}
/*
Map clicked at: 20.98085354867591, 105.78798040202281
Step: [105.789153, 20.976631]
Step: [105.786798, 20.979057]
Step: [105.79009, 20.981821]
Step: [105.788306, 20.980714]
Step: [105.78812, 20.98095]
 */