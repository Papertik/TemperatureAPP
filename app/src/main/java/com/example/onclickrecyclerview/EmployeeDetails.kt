package com.example.onclickrecyclerview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.example.onclickrecyclerview.databinding.EmployeeDetailsBinding
import com.example.onclickrecyclerview.ui.theme.Settings
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class EmployeeDetails : AppCompatActivity() {
    //============================================================================================
    private var emplist: Employee? = null
    private var binding: EmployeeDetailsBinding? = null
//    private lateinit var adapter: ItemAdapter
//    private val tsDataList = mutableListOf<TSData>()

    // function for gettting data from thingspeaks .json file
    private fun fetchDataFromThingSpeak(ChannelID: String?, fieldnum: String?): Double {
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
                                    return@withContext fieldValue.toDouble() // Attempt to convert to Double
                                } catch (e: NumberFormatException) {
                                    Log.e("ThingSpeak Addy", "Error converting to Double: ${e.message}") // this will pass an error for every feild it finds null in
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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onResume()
        binding = EmployeeDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
//============================================================================================
        //Handling button clicks n stuff
        val imagebuttonClick = findViewById<ImageButton>(R.id.HomeButton)
        imagebuttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
        imagebutton1Click.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }


        if (intent.hasExtra(MainActivity.NEXT_SCREEN)) {
            emplist = intent.getSerializableExtra(MainActivity.NEXT_SCREEN) as Employee
        }
//============================================================================================
// Getting data from thingspeak
        // first time data collectiion ( checks every 15 min)
        val handler = Handler(Looper.getMainLooper())


        // function for modifying data every hour.

        val maxDataPoints = 96
        val maxDataPointsYR = 365
        val hourTemp = mutableListOf<Double>()
        val HourTime = mutableListOf<Int>()
        val dayTemp = mutableListOf<Double>()
        val dayTime = mutableListOf<Int>()

        fun updateGraphData() {
            if (emplist != null){
            binding?.displayName?.text = emplist!!.name
            val TSdata = fetchDataFromThingSpeak(emplist!!.channel, emplist!!.field)
            binding?.displayEmail?.text = TSdata.toString()
            hourTemp.add(TSdata)}

            //doin stuff with the hourly graph
            HourTime.clear()
            for (i in 1..minOf(hourTemp.size, maxDataPoints)) {
                HourTime.add(i) }
            if (hourTemp.size > maxDataPoints) {
                hourTemp.removeAt(0)}


//============================================================================================
            //Plotting data, hopefully
            val lineChart: LineChart = findViewById(R.id.lineChart)
            val customColors = intArrayOf(R.color.white,R.color.yellow,R.color.BarBlue,R.color.backgroundBlue            )
            val entries = mutableListOf<Entry>()

            for (i in HourTime.indices) {
                entries.add(Entry(HourTime[i].toFloat(), hourTemp[i].toFloat()))
            }

            val dataSet = LineDataSet(entries, "HourTemp vs HourTime")
            dataSet.setColors(intArrayOf(R.color.yellow), this) // Specify your yellow color resource

            val textcolor = 0
// Customize X Axis (bottom)
            lineChart.xAxis.textColor = customColors[textcolor]

// Customize Y Axis (left)
            lineChart.axisLeft.textColor = customColors[textcolor]

// Hide the right Y Axis
            lineChart.axisRight.isEnabled = false

            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(dataSet)
            val lineData = LineData(dataSets)
            lineChart.data = lineData

// Beautification
// lineChart.description.text = "Your Chart Description" // Chart title
            lineChart.setTouchEnabled(true)
            lineChart.isDragEnabled = true
            lineChart.setScaleEnabled(true)

// Finally, refresh the chart to apply the changes
            lineChart.invalidate()
 }

        updateGraphData()
        //============================================================================================
        fun creatingDayData() {
            // averaging the hourly data with to make a day avg and then creating a corrospoinding time for that, we'll see if this is used
            // if the yearly graph is going to be used, we need to make sure it only happens once per day and not once per hour, so new timer block
            var sum = 0.0
            var avg = 0.0
            for (value in hourTemp) {
                sum += value
                avg = sum / hourTemp.size
            }
            dayTemp.add(avg)

            dayTime.clear()
            for (i in 1..dayTemp.size) {
                dayTime.add(i)
            }

            if (dayTemp.size > maxDataPointsYR) {
                // Remove the oldest data point
                dayTemp.removeAt(0)
            }
        }
        // things that happen every hour
        handler.postDelayed({updateGraphData() }, 900020)// 3600000 for an hour, currently set to update graph and temperature every 15 min
        // things that happen every day
        handler.postDelayed({creatingDayData()}, 86400000 )

//============================================================================================
        // Deleteing sensors
        // when delete button is clicked it finds the ID of the sensor, then deletes it using the delete function in the object class. Then displays  toast message and returens to main activity manually.
        val deleteButton = findViewById<Button>(R.id.DeleteButton)
        deleteButton.setOnClickListener {
            val emplist: Employee? =
                intent.getSerializableExtra(MainActivity.NEXT_SCREEN) as? Employee
            // Assuming emplist is not null
            val employeeId = emplist?.id ?: -1
            EmployeeInfo.deleteEmployee(employeeId)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(
                this@EmployeeDetails,
                "Sensor deleted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onResume() {
        super.onResume()
        if (emplist != null){
            binding?.displayName?.text = emplist!!.name
            val TSdata = fetchDataFromThingSpeak(emplist!!.channel, emplist!!.field)
            binding?.displayEmail?.text = TSdata.toString()
//            tsDataList.add(TSData(emplist!!.name, TSdata))
//            adapter.notifyDataSetChanged()
        }
    }

}
