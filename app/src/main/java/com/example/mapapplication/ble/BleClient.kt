package com.example.mapapplication.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.Closeable

@SuppressLint("MissingPermission")
class BleClient(
    val context: Context,
    val device: BluetoothDevice,
) : Closeable {
    var gatt: BluetoothGatt? = null
    var gattService: BluetoothGattService? = null

    private var callback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                gatt?.discoverServices()
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.services?.forEach { service ->
                    Log.d("Bluetooth", "Service UUID: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.d("Bluetooth", "  └─ Characteristic UUID: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.e("Bluetooth", "Service discovery failed with status $status")
            }
        }
    }

    fun connect() {
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, callback)
        }
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}