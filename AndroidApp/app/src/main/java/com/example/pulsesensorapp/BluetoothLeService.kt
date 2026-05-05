package com.example.pulsesensorapp

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID

private const val LOG_TAG = "BluetoothLeService"
private const val SCAN_PERIOD: Long = 10000
private const val DEVICE_NAME = "Pulsoxymeter"
private val CHAR_UUID = UUID.fromString("BF3FBD80-063F-11E5-9E69-0002A5D5C501")

class BluetoothLeService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    private var bluetoothAdapter: BluetoothAdapter? = null

    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private var bluetoothGatt: BluetoothGatt? = null

    private var characteristic: BluetoothGattCharacteristic? = null

    private val binder = LocalBinder()

    var scanning = false

    private var connectionState = State.DISCONNECTED
    var device: BluetoothDevice? = null

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(LOG_TAG, "Found not matched device: ${result.scanRecord?.deviceName}")
            if (result.scanRecord?.deviceName == DEVICE_NAME) {
                Log.i(LOG_TAG, "Found device: ${result.scanRecord?.deviceName}")
                stopDeviceScan()
                connectToDevice(result.device)
            }
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcastUpdate(ACTION_GATT_CONNECTED)
                connectionState = State.CONNECTED
                Log.i(LOG_TAG, "Connected to GATT server.")
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
                connectionState = State.DISCONNECTED
                Log.i(LOG_TAG, "Disconnected from GATT server.")
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(LOG_TAG, "Discovered services: ${gatt?.services}")
            val characteristic =
                gatt!!.services.flatMap { it.characteristics }.find { it.uuid == CHAR_UUID }

            if (characteristic != null) {
                Log.d(LOG_TAG, "Found characteristic: ${characteristic.uuid}")
                this@BluetoothLeService.characteristic = characteristic
                broadcastUpdate(ACTION_FOUND_CHARACTERISTIC)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.uuid == CHAR_UUID && value.size == 2) {
                Log.d(LOG_TAG, "Heart Rate: ${value[0]}, Oxygen: ${value[1]}")
                broadcastUpdate(ACTION_READ_CHARACTERISTIC, value)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun initialize(): Boolean {
        bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        if (bluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun connect() {
        scanLeDevice()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun readHeartRate() {
        bluetoothGatt?.readCharacteristic(this.characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun scanLeDevice() {
        if (!scanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
            Log.d(LOG_TAG, "Scanning for devices")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopDeviceScan() {
        if (scanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
            Log.d(LOG_TAG, "Stop Scanning for devices")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice(newDevice: BluetoothDevice) {
        this.device = newDevice
        Log.d(LOG_TAG, "Connecting to Gatt: ${newDevice.name}")
        bluetoothGatt = device?.connectGatt(this, false, bluetoothGattCallback)
    }

    private fun broadcastUpdate(action: String, data: ByteArray? = null) {
        val intent = Intent(action)
        if (data != null) {
            intent.putExtra(EXTRA_DATA, data)
        }
        sendBroadcast(intent)
    }


    companion object {
        private enum class State {
            DISCONNECTED, CONNECTED
        }

        const val ACTION_GATT_CONNECTED = "com.example.pulsesensorapp.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.pulsesensorapp.ACTION_GATT_DISCONNECTED"
        const val ACTION_FOUND_CHARACTERISTIC =
            "com.example.pulsesensorapp.ACTION_FOUND_CHARACTERISTIC"
        const val ACTION_READ_CHARACTERISTIC =
            "com.example.pulsesensorapp.ACTION_READ_CHARACTERISTIC"
        const val EXTRA_DATA = "com.example.pulsesensorapp.EXTRA_DATA"
    }
}