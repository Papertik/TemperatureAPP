package com.example.onclickrecyclerview

import android.app.Activity
import android.content.Intent
import android.health.connect.datatypes.units.Temperature
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onclickrecyclerview.EmployeeInfo.addEmployeeToDataList
import com.example.onclickrecyclerview.EmployeeInfo.writeEmployeesToCsv
import com.example.onclickrecyclerview.databinding.MainActivityBinding
import com.example.onclickrecyclerview.ui.theme.DeviceAdd
import com.example.onclickrecyclerview.ui.theme.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow
import kotlin.math.round

// In this project we are going to use view binding



class MainActivity : AppCompatActivity(), ItemAdapter.OnDeleteClickListener {
    fun fetchDataFromThingSpeak(ChannelID: String?, fieldnum: String?): Double {
        return runBlocking {
            withContext(Dispatchers.IO) {
                if (!ChannelID.isNullOrEmpty() or !fieldnum.isNullOrEmpty()) {
                    try {
                        // sample URL
                        val url = URL("https://api.thingspeak.com/channels/$ChannelID/field/$fieldnum.json")
                        val connection = url.openConnection() as HttpURLConnection

                        val inputStream = connection.inputStream
                        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                        val jsonResponse = bufferedReader.readText()

                        val jsonObject = JSONObject(jsonResponse)
                        val feeds = jsonObject.getJSONArray("feeds")

                        for (i in feeds.length() - 1 downTo 0) {
                            val currentFeed = feeds.getJSONObject(i)
                            val fieldValue = currentFeed.optString("field$fieldnum")
                            Log.d("ThingSpeak Addy", url.toString())

                            // Check if the field is not null
                            if (fieldValue.isNotEmpty()) {
                                try {
                                    val temperature= fieldValue.toDouble()
                                    return@withContext temperature.round(1)  // Attempt to convert to Double
                                } catch (e: NumberFormatException) {
//                                    Log.e("ThingSpeak Addy", "Error converting to Double: ${e.message}") // this will pass an error for every feild it finds null in
                                }
                            }
                        }

                        // Default value if no non-empty feeds are found
                        return@withContext 0.1
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("FetchData", "Exception: ${e.message}")
                        return@withContext 0.2 // Error for network issues or JSON parsing issues
                    }
                } else {
                    return@withContext 0.3 // Input field is empty
                }
            }
        }
    }
    fun Double.round(decimals: Int): Double {
        val multiplier = 10.0.pow(decimals)
        return round(this * multiplier) / multiplier
    }
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val NAME = data?.getStringExtra("NAME") ?: "No name provided"
                val Channel = data?.getStringExtra("Channel") ?: "No value Provided"
                val FieldID= data?.getStringExtra("Field") ?: "No value Provided"
                var Temperature = fetchDataFromThingSpeak(Channel,FieldID)

                val newEmployee = addEmployeeToDataList(NAME, Channel, FieldID, Temperature)
                SensorList.add(newEmployee)
                (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(SensorList)
            }
        }

    // View Binding

    //====================================================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called")

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
//            val NAME = data?.getStringExtra("NAME") ?: "No name provided"
//            val Channel = data?.getStringExtra("TSChannel") ?: "No value Provided"
//            val FieldID= data?.getStringExtra("TSField") ?: "No value Provided"



            // Update RecyclerView adapter with the new data
            (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(SensorList)
        }
    }
    var binding:MainActivityBinding?=null
    private var SensorList: ArrayList<Employee> = ArrayList()
    private lateinit var adapter: ItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // settings button to go to settings
        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
        imagebutton1Click.setOnClickListener {
            saveData()
            val intentSettings = Intent(this, Settings::class.java)
            startActivity(intentSettings)
        }

// CODE FOR REFRESHING MAIN PAGE UI
//        val refreshclick = findViewById<ImageButton>(R.id.refresh)
//        refreshclick.setOnClickListener{
//            adapter.updateData(EmployeeInfo.getEmployeeData())
//        }

        binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
        binding?.rvItemsList?.setHasFixedSize(true)

        // Creating an instance of the
        // adapter and passing emplist to it
        adapter = ItemAdapter(EmployeeInfo.getEmployeeData(), object : ItemAdapter.OnDeleteClickListener {
            override fun onDeleteClick(employee: Employee) {
                // Handle delete click here
                EmployeeInfo.deleteEmployee(employee.id)
                adapter.notifyDataSetChanged()
                saveData()
            }
        })
        loadContent()
        saveData()
        binding?.rvItemsList?.adapter = adapter

        // Applying OnClickListener to our Adapter
        adapter.setOnClickListener(object :
            ItemAdapter.OnClickListener {
            override fun onClick(position: Int, model: Employee) {
                val intent = Intent(this@MainActivity, EmployeeDetails::class.java)
                // Passing the data to the
                // EmployeeDetails Activity
                intent.putExtra(NEXT_SCREEN, model)
                startActivity(intent)
                saveData()
            }
        })
        binding?.addbutton?.setOnClickListener {
            startDeviceAddActivity()
        }
    }
    //====================================================

    // deleet shtuff
    override fun onDeleteClick(employee: Employee) {
        // Handle the delete action here
        // You can show a toast message if needed
        val position = SensorList.indexOf(employee)
        if (position != -1) {
            SensorList.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, "Employee ${employee.name} deleted", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("MainActivity", "Employee not found in the list")
        }
    }

    private fun startDeviceAddActivity() {
        val intent = Intent(this, DeviceAdd::class.java)
        startForResult.launch(intent)
    }
    private fun loadContent() {
        isRefreshing = false
        val path = applicationContext.filesDir
        val readFrom = File(path, "SensorData.csv")
        val content = ByteArray(readFrom.length().toInt())

        var stream: FileInputStream? = null
        try {
            stream = FileInputStream(readFrom)
            stream.read(content)

            val csvString = String(content)
            // Check if the CSV string is empty
            if (csvString.isNotEmpty()) {
                val lines = csvString.split("\n")
                val sensorList: MutableList<Employee> = mutableListOf()

                for (line in lines) {
                    val values = line.split(",") // Adjust the delimiter based on your CSV format

                    // Ensure the line has enough values
                    if (values.size >= 5) {
                        val employee = Employee(
                            id = values[0].toInt(),
                            name = values[1],
                            channel = values[2],
                            field = values[3],
                            temperature = values[4].toDouble(),
                        )
                        SensorList.add(employee)
                    }
                }


                // Assuming you have an existing adapter named adapter in your MainActivity
                adapter.updateData(SensorList)
                Log.d("Load Data", "DataLoaded")
                for (employee in SensorList) {
                    Log.d(
                        "Employee Data",
                        "ID: ${employee.id}, Name: ${employee.name}, Channel: ${employee.channel}, Field: ${employee.field}, Temperauture: ${employee.temperature}"
                    )
                    isRefreshing = true
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
    }
    private var isRefreshing = true // Flag to control coroutine execution

//    private fun fetchAndUpdateTemp() {
//        CoroutineScope(Dispatchers.IO).launch {
//            // Perform background tasks, such as network operations
//            while(isRefreshing) {
//                Log.d("FetchAndUpdateTemp", "Running...")
//                Log.d("FetchingData", "$SensorList")
//
//                for (employee in SensorList) {
//                    val temperature = fetchDataFromThingSpeak(employee.channel, employee.field)
//                    employee.temperature = temperature
//                    Log.d("FetchAndUpdateTemp", "Employee: ${employee.name}, Temperature: $temperature")
//
//                }
//            // Switch to the main thread to update the UI
//            withContext(Dispatchers.Main) {
//                adapter.notifyDataSetChanged()
//                delay(1000)
//            }}
//        }
//    }


    private fun saveData() {
        val path: File = applicationContext.filesDir
        val file = File(path, "SensorData.csv")
        try {
            if (file.exists()) {
                writeEmployeesToCsv(file, SensorList)
                adapter.updateData(SensorList)
                Log.d("Save Data", "SensorData.csv successfully written")
            }
//            val filePath = File(filesDir, "SensorData.csv").absolutePath
//            Log.d("FilePath", "SensorData.csv saved at: $filePath")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun onDestroy() {
        isRefreshing = false
        saveData()
        super.onDestroy()
        binding = null
    }
    override fun onPause() {
        isRefreshing = false
        saveData()
        super.onPause()
        // Save the data when the activity loses focus
    }

//    override fun onResume() {
//        super.onResume()
//        isRefreshing = true
//        fetchAndUpdateTemp()
//
//    }
    override fun onBackPressed() {
        super.onBackPressed()
        saveData()
        // Save the data when the back button is pressed
    }
    companion object{
        const val NEXT_SCREEN="details_screen"
    }
}
