package com.example.pulsesensorapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.pulsesensorapp.ui.components.HeartRateMonitor
import com.example.pulsesensorapp.ui.theme.PulseSensorAppTheme

private const val REQUEST_ENABLE_BT = 42
private const val LOG_TAG = "MainActivity"
private const val POLL_INTERVAL = 500L

class MainActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var connectedState = mutableStateOf(false)
    private var heartRateState = mutableIntStateOf(0)
    private var oxygenState = mutableIntStateOf(0)


    private var bluetoothService: BluetoothLeService? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            startBluetoothLeService()
        } else {
            Log.w(LOG_TAG, "Bluetooth permissions were not granted.")
            finish()
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onServiceConnected(
            componentName: ComponentName, service: IBinder
        ) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(LOG_TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                bluetooth.connect()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connectedState.value = true
                }

                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connectedState.value = false
                    handler.removeCallbacks(pollRunnable)
                    bluetoothService?.connect()
                }

                BluetoothLeService.ACTION_FOUND_CHARACTERISTIC -> {
                    handler.post(pollRunnable)
                }

                BluetoothLeService.ACTION_READ_CHARACTERISTIC -> {
                    val data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA)
                    if (data?.size == 2) {
                        heartRateState.intValue = data[0].toUByte().toInt()
                        oxygenState.intValue = data[1].toUByte().toInt()
                    }
                }
            }
        }
    }

    private val pollRunnable = object : Runnable {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun run() {
            bluetoothService?.readHeartRate()
            handler.postDelayed(this, POLL_INTERVAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseSensorAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HeartRateMonitor(
                        connected = connectedState.value,
                        modifier = Modifier.padding(innerPadding),
                        heartRate = heartRateState.intValue,
                        oxygen = oxygenState.intValue,
                    )
                }
            }
        }
        askForBluetoothPermission()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(), RECEIVER_EXPORTED)
        if (connectedState.value) {
            handler.removeCallbacks(pollRunnable)
            handler.post(pollRunnable)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
        handler.removeCallbacks(pollRunnable)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_ENABLE_BT) {
            return
        }
        if (resultCode != RESULT_OK) {
            Log.w(LOG_TAG, "Bluetooth was not enabled.")
            finish()
            return
        }

        startBluetoothLeService()
    }

    private fun askForBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val missingPermissions = permissions.filter {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                startBluetoothLeService()
            }
        } else {
            startBluetoothLeService()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothLeService() {
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter ?: return
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
            bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_FOUND_CHARACTERISTIC)
            addAction(BluetoothLeService.ACTION_READ_CHARACTERISTIC)
        }
    }
}