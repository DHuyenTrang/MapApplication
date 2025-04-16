package com.example.mapapplication.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mapapplication.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.mapapplication.R
import com.example.mapapplication.TokenManager
import com.example.mapapplication.Utils.moveCameraToLocation
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.map4d.types.MFLocationCoordinate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tokenManager: TokenManager by inject()
    private val currentLocationViewModel: CurrentLocationViewModel by viewModel()
    private val routeViewModel: RouteViewModel by viewModel()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // API Google Play Services giup dinh vi
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, // priority
        3000L // interval in milliseconds
    ).apply {
        setMinUpdateIntervalMillis(1000L) // fastest interval
    }.build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val controller = navHostFragment.navController

//        if (checkCurrentUser()) {
//            controller.navigate(R.id.mapFragment)
//        } else {
//            controller.navigate(R.id.signInFragment)
//        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val location = result.lastLocation ?: return
            Log.d("LOCATION", "Location: ${location.latitude}, ${location.longitude}")
            updateCurrentLocationOnMap(location)
        }
    }

    private fun updateCurrentLocationOnMap(location: Location) {
        currentLocationViewModel.setCurrentLocation(location)
    }

    private fun requestLocationPermission() {
        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if (isGranted) {
                Log.d("PERMISSION", "Location permission granted")
            } else {
                Log.d("PERMISSION", "Location permission denied")
            }
        }

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        else{
            requestPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkCurrentUser(): Boolean {
        Log.d("AUTH", "checkCurrentUser: ${tokenManager.getAccessToken()}")
        return tokenManager.getUserId() != null
    }
}
