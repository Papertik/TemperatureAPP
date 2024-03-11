package com.example.onclickrecyclerview

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.updateTransition
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onclickrecyclerview.databinding.MainActivityBinding
import com.example.onclickrecyclerview.ui.theme.DeviceAdd
import com.example.onclickrecyclerview.ui.theme.WIfiSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.function.DoubleBinaryOperator

// In this project we are going to use view binding
class MainActivity : AppCompatActivity(), ItemAdapter.OnDeleteClickListener {
    companion object {
        const val NEXT_SCREEN = "details_screen"
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val NAME = data?.getStringExtra("NAME") ?: "No name provided"
                val Channel = data?.getStringExtra("Channel") ?: "No value Provided"
                val FieldID = data?.getStringExtra("Field") ?: "No value Provided"
                // Fetch the list of temperatures using suspend function
                val temperatureList = runBlocking {
                    SensorInfo.fetchDataFromThingSpeak(Channel, FieldID)
                }
                val recentTemperature = SensorInfo.getMostRecentTemperature(temperatureList)
                val newsensor = Sensor(
                    id = homeScreenSensors.size + 1,
                    name = NAME,
                    channel = Channel,
                    field = FieldID,
                    tempList = temperatureList.toMutableList(),
                    temperature = recentTemperature
                )
                homeScreenSensors.add(newsensor)
                (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(homeScreenSensors)
            }
        }

    // View Binding
    var binding: MainActivityBinding? = null
    private var homeScreenSensors: ArrayList<Sensor> = ArrayList()
    private lateinit var adapter: ItemAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val updateIntervalMillis: Long = 15 * 60 * 1000 // 15 minutes in milliseconds


    //====================================================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Update RecyclerView adapter with the new data
            adapter.updateData(homeScreenSensors)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // settings button to go to settings

        binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
        binding?.rvItemsList?.setHasFixedSize(true)
        // Creating an instance of the adapter and passing emplist to it

        // handling delete click in item adapter
        adapter =
            ItemAdapter(SensorInfo.getsensorData(), object : ItemAdapter.OnDeleteClickListener {
                override fun onDeleteClick(sensor: Sensor) {
                    // Handle delete click here
                    SensorInfo.deletesensor(sensor.id)
                    adapter.notifyDataSetChanged()
                }
            })
        adapter.setOnClickListener(object :
            ItemAdapter.OnClickListener {
            override fun onClick(position: Int, model: Sensor) {
                val intent = Intent(this@MainActivity, sensorDetails::class.java)
                // Passing the data to the sensorDetails Activity
                intent.putExtra(NEXT_SCREEN, model)
                startActivity(intent)
                //saveData()
            }
        })
        binding?.rvItemsList?.adapter = adapter

        loadContent() //<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<- LOAD
        schedulePeriodicUpdates()//update homescreen temp display every minute

//        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
//        imagebutton1Click.setOnClickListener {
//            saveData()
//            val intentSettings = Intent(this, Settings::class.java)
//            startActivity(intentSettings)
//        }
        // Handling poppup menu logic
        binding?.MenuBtn?.setOnClickListener { button ->
            val setMenu = PopupMenu(this@MainActivity, button)
            setMenu.menuInflater.inflate(R.menu.settings_menu, setMenu.menu)
            setMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.Wifi -> {
                        // Open WifiActivity
                        val intent = Intent(this@MainActivity, WIfiSettings::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.AddSensor -> {
                        // Open AddSensorActivity
                        startDeviceAddActivity()
                        true
                    }

                    R.id.Clear -> {
                        // Open ClearSensorsActivity
                        clearData(homeScreenSensors)
                        true
                    }

                    else -> false
                }
            }
            setMenu.show()
        }
    }
    //====================================================

    // deleet shtuff
    // handling delete click in main activity
    override fun onDeleteClick(sensor: Sensor) {
        val position = homeScreenSensors.indexOf(sensor)
        if (position != -1) {
            homeScreenSensors.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveData()
            Toast.makeText(this, "sensor ${sensor.name} deleted", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("MainActivity", "sensor not found in the list")
        }
    }

    private fun startDeviceAddActivity() {
        val intent = Intent(this, DeviceAdd::class.java)
        startForResult.launch(intent)
    }

    //
    //====================================================
    // functions to handle data persistance
    private fun loadContent() {
        val path = applicationContext.filesDir
        val readFrom = File(path, "SensorData.csv")
        val content = ByteArray(readFrom.length().toInt())
        if (readFrom.exists()) {
            var stream: FileInputStream? = null
            try {
                stream = FileInputStream(readFrom)
                stream.read(content)

                val csvString = String(content)
                // Check if the CSV string is empty
                if (csvString.isNotEmpty()) {
                    val lines = csvString.split("\n")

                    for (line in lines) {
                        val values = line.split(",")

                        // Ensure the line has enough values
                        if (values.size >= 6) { // Assuming tempList is at index 5
                            val fetchedsensor = Sensor(
                                id = values[0].toInt(),
                                name = values[1],
                                channel = values[2],
                                field = values[3],
                                tempList = mutableListOf(),
                                temperature = 0.0 // Initialize with a default value, you may adjust this
                            )

                            val temperatureList = runBlocking {
                                SensorInfo.fetchDataFromThingSpeak(fetchedsensor.channel, fetchedsensor.field)
                            }
                            val recentTemperature = SensorInfo.getMostRecentTemperature(temperatureList.reversed())

                            // Update the fetched sensor with the temperature list and recent temperature
                            fetchedsensor.tempList=(temperatureList)
                            fetchedsensor.temperature = recentTemperature

                            homeScreenSensors.add(fetchedsensor)
                        }
                    }

                    // Assuming you have an existing adapter named adapter in your MainActivity
                    adapter.updateData(homeScreenSensors)
                    Log.d("Load Data", "DataLoaded")
                    for (sensor in homeScreenSensors) {
                        updateHomeTemp()
                        Log.d(
                            "sensor Data",
                            "ID: ${sensor.id}, Name: ${sensor.name}, Channel: ${sensor.channel}, Field: ${sensor.field}, Hist data: ${sensor.tempList}"
                        )
                    }
//                val file = File(filesDir, "SensorData.csv")
//                if (file.exists()) {
//                    file.delete()
//                }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    stream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (!readFrom.exists()) {
            Log.d("Load Content", "File doesn't exist")
        }
    }

    private fun saveData() {
        Log.d("Save Data", "Saving data...")

        val path: File = applicationContext.filesDir
        val file = File(path, "SensorData.csv")

        try {
            if (file.exists()) {
                Log.d("Save Data", "File exists and SensorList is not empty")
                SensorInfo.writesensorsToCsv(file, homeScreenSensors)
                Log.d("Save Data", "SensorData.csv successfully written")
            } else {
                Log.d("Save Data", "File does not exist or SensorList is empty")

                // Create the file if it doesn't exist
                if (!file.exists()) {
                    val created = file.createNewFile()
                    Log.d("Save Data", "SensorData.csv created: $created")
                }
            }
        } catch (e: Exception) {
            Log.e("Save Data", "Error in saveData(): ${e.message}")
            e.printStackTrace()
        }
    }

    private fun clearData(tobeCleared: ArrayList<Sensor>) {
        // Build the confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to clear all data?")

        // Set up the buttons
        builder.setPositiveButton("Yes") { _, _ ->
            for (sensor in tobeCleared) {
                SensorInfo.deletesensor(sensor.id)
            }
            val path: File = applicationContext.filesDir
            val file = File(path, "SensorData.csv")
            if (file.exists()) {
                file.delete()
                recreate()
                adapter.notifyDataSetChanged()


            } else {
                Log.d("ClearData", "File Does not exist")
            }

            Toast.makeText(this, "Data cleared", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") { _, _ ->
            // User clicked No, do nothing
        }

        // Show the dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun schedulePeriodicUpdates() {
        handler.postDelayed({
            updateHomeTemp()
            schedulePeriodicUpdates()
        }, updateIntervalMillis)
    }

    private fun updateHomeTemp() {
        lifecycleScope.launch {
            for (sensor in homeScreenSensors) {
                val temperatureList = SensorInfo.fetchDataFromThingSpeak(sensor.channel, sensor.field)
                // Get the most recent temperature from the list
                val newTemp = SensorInfo.getMostRecentTemperature(temperatureList.reversed())
                adapter.updateTemperature(sensor.id, newTemp)
            }
        }
    }

    override fun onDestroy() {
        saveData()
        super.onDestroy()
        binding = null
    }
    override fun onPause() {
//        saveData()         // Save the data when the activity loses focus
        super.onPause()

    }
}
