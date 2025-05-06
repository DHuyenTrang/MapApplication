package com.example.mapapplication.ui.map

import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mapapplication.R
import com.example.mapapplication.utils.Utils.moveCameraToLocation
import com.example.mapapplication.databinding.FragmentMapBinding
import com.example.mapapplication.utils.extension.animateCameraSmoothlyTo
import com.example.mapapplication.utils.extension.animateToPosition
import com.example.mapapplication.utils.extension.drawMarker
import com.example.mapapplication.utils.extension.toKmPerHour
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFBuildingOptions
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
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

    private var lastCameraPosition: MFLocationCoordinate? = null
    private var lastCameraBearing: Float? = null
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
        setUpBottomSheet()
    }

    private fun setUpBottomSheet() {
        val bottomSheet = binding.bottomSheetDashboard.root
        val bottomSheetBehavior =
            BottomSheetBehavior.from<LinearLayout>(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun listenEvent() {
        binding.btnMyLocation.setOnClickListener {
            setUpCurrentLocation()
        }
        binding.bottomSheetDashboard.searchBar.setOnClickListener {
            findNavController().navigate(R.id.searchLocationFragment)
        }
    }

    private fun setUpCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentLocationViewModel.currentLocation
                .debounce(500)
                .collectLatest {location ->
                if (location != null) {
                    currentLocationMarker = map4D.drawMarker(currentLocationMarker, location.latitude, location.longitude, R.drawable.ic_location)
                    currentLocationMarker?.animateToPosition(location)

                    val currentSpeed = location.speed.toKmPerHour()
                    binding.txtCurrentSpeed.text = currentSpeed.toString()
                    val current = MFLocationCoordinate(location.latitude, location.longitude)
                    val from = lastCameraPosition ?: current

                    map4D.animateCameraSmoothlyTo(
                        from,
                        current,
                        location.bearing.toDouble(),
                        17.0,
                        60.0
                    )
                    lastCameraPosition = current
//                    if(currentSpeed > 10){
//                        moveCameraToLocation(map4D, location.latitude, location.longitude, 25.0, 60.0, location.bearing)
//                    }
//                    else {
//                        moveCameraToLocation(map4D, location.latitude, location.longitude, 14.5, 0.0, location.bearing)
//                    }

                } else {
                    moveCameraToLocation(map4D, 20.98085354867591, 105.78798040202281, 14.5, 0.0, 0f)
                }
            }
        }
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.MAP3D
            map4D.setTiltGesturesEnabled(true)
            setUpCurrentLocation()

//            val buildingOptions = MFBuildingOptions()
//            buildingOptions.location(MFLocationCoordinate(20.98085354867591, 105.78798040202281))
//                .name("Test Building")
//                .model("https://ucarecdn.com/2201007e-6f2e-47b4-8ff0-a4e413741139/Lowpoly_tree_sample.obj")
//
//            map4D.addBuilding(buildingOptions)
        }

    }
}