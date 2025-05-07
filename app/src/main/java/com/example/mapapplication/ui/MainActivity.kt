package com.example.mapapplication.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.mapapplication.R
import com.example.mapapplication.databinding.ActivityMainBinding
import com.example.mapapplication.manager.TokenManager
import com.example.mapapplication.utils.Constant // Make sure Constant.BLUETOOTH_REQUEST_CODE exists or remove if not used elsewhere
import com.example.mapapplication.utils.extension.toKmPerHour
import com.example.mapapplication.viewmodel.CurrentLocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tokenManager: TokenManager by inject()
    private val currentLocationViewModel: CurrentLocationViewModel by viewModel()

    private var previousLocation: Location? = null
    private var currentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, // priority
        1000L // interval in milliseconds
    ).apply {
        setMinUpdateIntervalMillis(1000L) // fastest interval
    }.build()

    // --- Permission Launchers ---
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
//    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<String>
//
//    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>

    // --- Location Callback ---
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            currentLocation = result.lastLocation ?: return
            Log.d("LOCATION", "New Location: ${currentLocation?.latitude}, ${currentLocation?.longitude}, Speed: ${currentLocation?.speed?.toKmPerHour()} km/h")

            if (previousLocation == null) {
                previousLocation = currentLocation
                updateCurrentLocationOnMap(currentLocation!!)
            } else {
                // Update only if moved a certain distance (e.g., 5 meters)
                if (currentLocation!!.distanceTo(previousLocation!!) >= 5f) {
                    previousLocation = currentLocation
                    updateCurrentLocationOnMap(currentLocation!!)
                } else {
                    Log.d("LOCATION", "Location unchanged significantly.")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeLogout()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        initializePermissionLaunchers()
        checkAndRequestLocationPermission()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val controller = navHostFragment.navController

        if (checkCurrentUser()) {
            controller.navigate(R.id.mapFragment)
        } else {
            controller.navigate(R.id.signInFragment)
        }
    }

    private fun observeLogout() {
        lifecycleScope.launch {
            tokenManager.logoutFlow.collectLatest {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                navHostFragment.navController.navigate(R.id.signInFragment)
            }
        }
    }

    private fun initializePermissionLaunchers() {
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("PERMISSION", "Location permission granted")
                startLocationUpdates()
//                checkAndRequestBluetoothPermission()
            } else {
                Log.d("PERMISSION", "Location permission denied")
            }
        }

//        bluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                checkAndEnableBluetooth()
//            }
//        }
//
//        bluetoothEnableLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK) {
//                Log.d("BLUETOOTH", "Bluetooth enabled by user.")
//            } else {
//                Log.d("BLUETOOTH", "User declined to enable Bluetooth.")
//            }
//        }
    }

    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
//                checkAndRequestBluetoothPermission()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null && currentLocation == null) {
                        Log.d("LOCATION", "Got last known location: ${location.latitude}, ${location.longitude}")
                        currentLocation = location
                        updateCurrentLocationOnMap(location)
                    }
                }

            } catch (e: SecurityException) {
                checkAndRequestLocationPermission()
            }
        }
    }

//    private fun checkAndRequestBluetoothPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            when {
//                ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) == PackageManager.PERMISSION_GRANTED -> {
//                    Log.d("PERMISSION", "Bluetooth Connect permission already granted.")
//                    checkAndEnableBluetooth()
//                }
//                else -> {
//                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
//                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
//                }
//            }
//        } else {
//            checkAndEnableBluetooth()
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun checkAndEnableBluetooth() {
//        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
//        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
//
//        if (bluetoothAdapter == null) {
//            Log.w("BLUETOOTH", "Device does not support Bluetooth")
//            return
//        }
//
//        if (!bluetoothAdapter.isEnabled) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    Log.w("BLUETOOTH", "Cannot request Bluetooth enable without BLUETOOTH_CONNECT permission.")
//                    return
//                }
//            }
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            try {
//                bluetoothEnableLauncher.launch(enableBtIntent)
//            } catch (e: SecurityException){
//                Log.e("BLUETOOTH", "SecurityException trying to enable Bluetooth: ${e.message}")
//            }
//
//        } else {
//            Log.d("BLUETOOTH", "Bluetooth is already enabled.")
//
//        }
//    }

    private fun updateCurrentLocationOnMap(location: Location) {
        currentLocationViewModel.setCurrentLocation(location)
        if (location.hasSpeed()) {
            currentLocationViewModel.setCurrentSpeed(location.speed.toKmPerHour())
        }
    }

    private fun checkCurrentUser(): Boolean {
        val userId = tokenManager.getUserId()
        return userId != null
    }

}