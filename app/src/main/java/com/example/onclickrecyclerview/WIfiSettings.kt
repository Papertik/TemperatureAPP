package com.example.onclickrecyclerview.ui.theme

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.core.app.ActivityCompat
import com.example.onclickrecyclerview.DeviceConnectionActivity
import com.example.onclickrecyclerview.MainActivity
import com.example.onclickrecyclerview.R
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class WIfiSettings : AppCompatActivity() {
    companion object{
        val EXTRA_ADRESS: String = "Device_adress"
        var PASS: String? = null
        var SSID: String? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_settings)

        var bt: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var m_bluetoothAdapter: BluetoothAdapter = bt.adapter
        lateinit var m_pairedDevices: Set<BluetoothDevice>
        val REQUEST_ENABLE_BLUETOOTH = 1


        val imagebuttonClick = findViewById<ImageButton>(R.id.HomeButton)
        imagebuttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // settings button to go to settings
//        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
//        imagebutton1Click.setOnClickListener {
//            val intent = Intent(this, Settings::class.java)
//            startActivity(intent)
//        }
        val espButton = findViewById<Button>(R.id.espBut)
        espButton.setOnClickListener {
            @Override
            fun pushESP(view: View?) {

                var ssidET = findViewById(R.id.ssid) as EditText
                var passET = findViewById(R.id.pass) as EditText

                SSID = ssidET.getText().toString()
                PASS = passET.getText().toString()
                CoroutineScope(Dispatchers.IO).launch {
                    pushToThingspeak(SSID!!, PASS!!)
                }
                println(SSID)
                println(PASS)
            }
        }

        val bluetoothLeScanner = m_bluetoothAdapter.bluetoothLeScanner
        var scanning = false
        val handler = Handler()
        var devicesList = mutableListOf<ScanResult>()
        val SCAN_PERIOD: Long = 1000
        val bluetoothList: ListView =
            findViewById(R.id.bluetoothList) // Replace with your ListView ID
        bluetoothList.setOnItemClickListener { parent, view, position, id ->
            val selectedDevice = devicesList[position]

            // Start the DeviceConnectionActivity and pass the selected device
            val intent = Intent(this, DeviceConnectionActivity::class.java)
            intent.putExtra("selectedDevice", selectedDevice.device)
            startActivity(intent)
        }
        // Adapter to manage the data for the ListView
        val adapter: ArrayAdapter<String> by lazy {
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                devicesList.map { it.device.name ?: "Unknown Device" })
        }

        fun updateListView() {
            // Update the adapter data
            val uniqueDevices = HashSet<BluetoothDevice>()

            // Filter out duplicate devices
            devicesList.forEach { scanResult ->
                val device = scanResult.device
                uniqueDevices.add(device)
            }

            // Update the adapter with the unique devices
            adapter.clear()
            uniqueDevices.forEach { uniqueDevice ->
                val deviceName = uniqueDevice.name ?: "Unknown Device"
                adapter.add(deviceName)
            }
            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged()
            // Set the adapter to the ListView
            bluetoothList.adapter = adapter
        }

        val leScanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Called when a new BLE device is discovered
                val device = result.device
                val rssi = result.rssi
                // Add the discovered device to your list
                devicesList.add(result)
                // Update your ListView with the devices
                updateListView()
            }
        }

        fun scanLeDevice() {
            if (!scanning) {
                handler.postDelayed({
                    scanning = false
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                    } else {
                        if (leScanCallback != null) {
                            bluetoothLeScanner.stopScan(leScanCallback!!)
                        }
                    }
                }, SCAN_PERIOD)
                scanning = true
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                } else {
                    bluetoothLeScanner.startScan(leScanCallback)
                }
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback!!)
            }
        }

        val blueButton = findViewById<Button>(R.id.select_device_refresh)
        blueButton.setOnClickListener {
            scanLeDevice()
        }


    }
    suspend fun pushToThingspeak(SSID: String, PASS: String) {
        val url = URL("https://api.thingspeak.com/update?api_key=G0J6BS1QK4ASIORU")
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            // Set the request method to GET
            urlConnection.requestMethod = "GET"

            // Build the query parameters
            val queryParams = "field1=$SSID&field2=$PASS"

            // Write the parameters to the output stream
            val outputStream = urlConnection.outputStream
            outputStream.write(queryParams.toByteArray())
            outputStream.close()

            // Get the response code
            val responseCode = urlConnection.responseCode

            // Process the response code if needed
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Do something with the successful response
            } else {
                // Handle the error response
            }
        } finally {
            // Disconnect the HttpURLConnection
            urlConnection.disconnect()
        }
    }

}