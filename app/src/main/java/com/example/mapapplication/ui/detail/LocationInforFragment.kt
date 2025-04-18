package com.example.mapapplication.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mapapplication.R
import com.example.mapapplication.utils.Utils
import com.example.mapapplication.databinding.FragmentLocationInforBinding
import com.example.mapapplication.utils.extension.drawMarker
import com.example.mapapplication.utils.extension.moveCameraToLocation
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        val bottomSheet = binding.bottomSheetDashboard.root
        val bottomSheetBehavior =
            BottomSheetBehavior.from<LinearLayout>(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        placeId = arguments?.getString("place_id") ?: ""
        Log.d("SearchLocation", "placeId: $placeId")

        locationDetailViewModel.fetchLocationDetail(placeId)
        currentLat = currentLocationViewModel.currentLocation.value?.latitude
        currentLng = currentLocationViewModel.currentLocation.value?.longitude

        binding.bottomSheetDashboard.btnRoute.setOnClickListener {
            findNavController().navigate(
                R.id.action_locationInforFragment_to_routeFragment,
                args = bundleOf(
                    "currentLat" to currentLat,
                    "currentLng" to currentLng,
                    "destinationLat" to destinationLat,
                    "destinationLng" to destinationLng,
                    "nameLocation" to binding.bottomSheetDashboard.tvNameLocation.text
                )
            )
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            locationDetailViewModel.locationDetail.collectLatest {
                if (it != null) {
                    destinationLat = it.lat
                    destinationLng = it.lng
                    Utils.moveCameraToLocation(map4D, it.lat, it.lng, 0.0)
                    destinationMarker = map4D.drawMarker(
                        destinationMarker,
                        it.lat,
                        it.lng,
                        R.drawable.ic_pin_marker
                    )
                    binding.bottomSheetDashboard.tvNameLocation.text = it.name
                    binding.bottomSheetDashboard.tvAddressLocation.text = it.address
                }
            }
        }
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
            currentLocationMarker = map4D.drawMarker(
                currentLocationMarker,
                currentLat!!,
                currentLng!!,
                R.drawable.ic_location
            )
            observe()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}