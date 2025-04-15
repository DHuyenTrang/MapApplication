package com.example.mapapplication.ui.detail

import android.location.Location
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
import com.example.mapapplication.model.LocationDetail
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vn.map4d.map.core.MFMapType
import vn.map4d.map.core.Map4D
import vn.map4d.map.core.OnMapReadyCallback
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.map4d.map.annotations.MFBitmapDescriptorFactory
import vn.map4d.map.annotations.MFMarker
import vn.map4d.map.annotations.MFMarkerOptions
import vn.map4d.types.MFLocationCoordinate

class LocationInforFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentLocationInforBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationDetailViewModel by viewModel()

    private lateinit var map4D: Map4D
    private var currentLocationMarker: MFMarker? = null
    private var destinationMarker: MFMarker? = null

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

        val placeId = arguments?.getString("place_id") ?: ""
        Log.d("SearchLocation", "placeId: $placeId")

        viewModel.fetchLocationDetail(placeId)

    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.locationDetail.collectLatest {
                if (it != null) {
                    setDestinationLocation(it)
                    binding.tvNameLocation.text = it.name
                    binding.tvAddressLocation.text = it.address
                }
            }
        }
    }

    private fun setDestinationLocation(locationDetail: LocationDetail) {
        val lat = locationDetail.lat
        val lng = locationDetail.lng
        Log.d("LocationDetail", "lat: $lat, lng: $lng")
        Utils.moveCameraToLocation(map4D, lat, lng, 0.0)
        if (destinationMarker == null) addMarkerToMap(lat, lng, R.drawable.ic_marker_destination)
        else{
            destinationMarker!!.remove()
            addMarkerToMap(lat, lng, R.drawable.ic_marker_destination)
        }
    }

    private fun setCurrentLocation(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        if(currentLocationMarker == null) addMarkerToMap(lat, lon, R.drawable.ic_location)
        else {
            currentLocationMarker!!.remove()
            addMarkerToMap(lat, lon, R.drawable.ic_location)
        }
    }

    private fun addMarkerToMap(lat: Double, lng: Double, source: Int) {
        destinationMarker = map4D.addMarker(
            MFMarkerOptions()
                .position(MFLocationCoordinate(lat, lng))
                .icon(MFBitmapDescriptorFactory.fromResource(source))
        )
    }

    override fun onMapReady(p0: Map4D?) {
        if (p0 != null) {
            map4D = p0
            map4D.mapType = MFMapType.ROADMAP
            observe()
        }
    }

}