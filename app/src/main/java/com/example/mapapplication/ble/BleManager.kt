package com.example.mapapplication.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class BleManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Trên Android 11 trở xuống không cần quyền này
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun createEnableBluetoothIntent(): Intent? {
        return if (!isBluetoothEnabled()) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        } else {
            null
        }
    }

    fun logBluetoothStatus() {
        when {
            !isBluetoothEnabled() -> Log.d("Bluetooth", "Bluetooth chưa được bật")
            else -> Log.d("Bluetooth", "Bluetooth đã được bật")
        }
    }
}
