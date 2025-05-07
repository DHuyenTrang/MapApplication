package com.example.mapapplication.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mapapplication.databinding.FragmentConnectGoSafeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectGoSafeFragment : Fragment() {
    private var _binding: FragmentConnectGoSafeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bleManager: BleManager

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothManager: BluetoothManager? = null

    private var bleClient: BleClient? = null

    val adapter = GoSafeAdapter {connectToDevice(it)}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentConnectGoSafeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val recyclerView = binding.listItemGofa
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        bleManager = BleManager(requireContext())

        initPermissionLaunchers()
        checkBluetoothPermissionAndEnable()
        if (bleManager.isBluetoothEnabled()) scanDevice()
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        Log.d("Bluetooth", "connectToDevice: ${device.name}")
        lifecycleScope.launch(Dispatchers.IO) {
            bleClient = BleClient(requireContext(), device)
            bleClient?.connect()
        }
    }

    @SuppressLint("MissingPermission")
    private val scanCallback: ScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            result?.device?.let { device ->
                if(device.name == null) return@let
                Log.d("ScanCallback", "onScanResult: ${device.name}")
                adapter.addDevice(device)
                adapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun scanDevice() {
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        var scanning = false
        val handler = Handler()
        val SCAN_PERIOD: Long = 3000

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(scanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(scanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    private fun initPermissionLaunchers() {
        bluetoothPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.all { it.value }
                if (granted) {
                    enableBluetooth()
                } else {
                    Toast.makeText(requireContext(), "Không có quyền Bluetooth", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        bluetoothEnableLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("Bluetooth", "Bluetooth đã được bật")
                    scanDevice()

                } else {
                    Toast.makeText(requireContext(), "Từ chối bật Bluetooth", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun checkBluetoothPermissionAndEnable() {
        if (!bleManager.hasBluetoothPermissions()) {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            bluetoothPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        if (!bleManager.isBluetoothEnabled()) {
            val intent = bleManager.createEnableBluetoothIntent()
            if (intent != null) {
                bluetoothEnableLauncher.launch(intent)

            }
        } else {
            Log.d("Bluetooth", "Bluetooth đã bật")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private inner class GattCallback: com.example.mapapplication.ble.GattCallback {}
}