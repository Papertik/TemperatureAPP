package com.example.onclickrecyclerview

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.nio.charset.Charset
import java.util.UUID

class DeviceConnectionActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var selectedDevice: BluetoothDevice
    private val context: Context
        get() {
            return(this);
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedDevice = intent.getParcelableExtra("selectedDevice")!!

        // Establish a connection with the selected device
        connectToDevice(selectedDevice)

    }

    private fun connectToDevice(device: BluetoothDevice) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Connect to the device and manage GATT operations
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permisAsions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothGatt = device.connectGatt(this, true, this.gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Looper.prepare()
            Toast.makeText(context, "Entered connection state change", Toast.LENGTH_SHORT).show();

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Connected to the device, now discover services
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(context, "Not connected", Toast.LENGTH_SHORT).show();
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Toast.makeText(context, "Entered Callback", Toast.LENGTH_SHORT).show();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Find the desired characteristic
                val SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
                val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
                val service = gatt?.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

                // Write your string as bytes to the characteristic
                val dataToSend = "Your String Data"
                val dataBytes = dataToSend.toByteArray(Charset.forName("UTF-8"))
                characteristic?.value = dataBytes
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                gatt?.writeCharacteristic(characteristic)
            } else {
                // Handle service discovery failure
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Toast.makeText(context, "Entered characteristic write", Toast.LENGTH_SHORT).show();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(ContentValues.TAG, "Characteristic write successful")
            } else {
                Log.e(ContentValues.TAG, "Characteristic write failed with status: $status")
            }
        }
    }
}
