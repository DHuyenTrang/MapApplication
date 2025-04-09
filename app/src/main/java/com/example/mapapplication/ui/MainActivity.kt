package com.example.mapapplication.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mapapplication.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.mapapplication.R
import com.example.mapapplication.TokenManager
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.example.mapapplication.viewmodel.RouteViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tokenManager: TokenManager by inject()
    private val currentLocationViewModel: CurrentLocationViewModel by viewModel()
    private val routeViewModel: RouteViewModel by viewModel()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // API Google Play Services giup dinh vi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val controller = navHostFragment.navController

        if (checkCurrentUser()) {
            controller.navigate(R.id.mapFragment)
        } else {
            controller.navigate(R.id.signInFragment)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
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
            getCurrentLocation()
        }
        else{
            requestPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
            currentLocationViewModel.setCurrentLocation(location)
        }
            .addOnFailureListener {
                currentLocationViewModel.setCurrentLocation(null)
            }
    }
    private fun checkCurrentUser(): Boolean {
        return tokenManager.getUserId() != null
    }
}
