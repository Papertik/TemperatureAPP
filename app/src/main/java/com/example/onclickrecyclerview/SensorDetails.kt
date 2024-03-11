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
import androidx.lifecycle.lifecycleScope
import com.example.onclickrecyclerview.databinding.EmployeeDetailsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class sensorDetails : AppCompatActivity() {
    //============================================================================================
    var emplist: Sensor? = null
    private var binding: EmployeeDetailsBinding? = null
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
//        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
//        imagebutton1Click.setOnClickListener {
//            val intent = Intent(this, Settings::class.java)
//            startActivity(intent)
//        }
        if (intent.hasExtra(MainActivity.NEXT_SCREEN)) {
            emplist = intent.getSerializableExtra(MainActivity.NEXT_SCREEN) as Sensor
        }
        updateGraph()
        // Deleteing sensors
        // when delete button is clicked it finds the ID of the sensor, then deletes it using the delete function in the object class. Then displays  toast message and returens to main activity manually.
        val deleteButton = findViewById<Button>(R.id.DeleteButton)
        deleteButton.setOnClickListener {
            val emplist: Sensor? =
                intent.getSerializableExtra(MainActivity.NEXT_SCREEN) as? Sensor
            // Assuming emplist is not null
            val sensorId = emplist?.id ?: -1
            SensorInfo.deletesensor(sensorId)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(
                this@sensorDetails,
                "Sensor deleted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
//============================================================================================
// Getting data from thingspeak
        // first time data collectiion ( checks every 15 min)


        // function for modifying data every hour.




//        //============================================================================================
//        fun creatingDayData() {
//            // averaging the hourly data with to make a day avg and then creating a corrospoinding time for that, we'll see if this is used
//            // if the yearly graph is going to be used, we need to make sure it only happens once per day and not once per hour, so new timer block
//
//            var sum = 0.0
//            var avg = 0.0
//            for (value in hourTemp) {
//                sum += value
//                avg = sum / hourTemp.size
//            }
//            dayTemp.add(avg)
//
//            dayTime.clear()
//            for (i in 1..dayTemp.size) {
//                dayTime.add(i)
//            }
//
//            if (dayTemp.size > maxDataPointsYR) {
//                // Remove the oldest data point
//                dayTemp.removeAt(0)
//            }
//        }
//        // calculating minutes to miliseconds
//        val minutes = 1 // change
//        val hrs = 1
//        val milimin: Long = minutes * 1000L * 60L
//        val milihours: Long = hrs * 1000L * 60 * 60
//        // things that happen every hour
//        handler.postDelayed({updateGraphData() }, milimin)// 3600000 for an hour, currently set to update graph and temperature every 15 min
//        // things that happen every day
//        handler.postDelayed({creatingDayData()}, milihours )

//============================================================================================

    private val maxDataPoints = 24
    val maxDataPointsYR = 365
    private val hourTime = mutableListOf<Double>()
    val dayTemp = mutableListOf<Double>()
    val dayTime = mutableListOf<Int>()
    private fun graphData() {
        // Assuming HourTime represents the x-axis and hourTemp represents y-axis

        val lineChart: LineChart = findViewById(R.id.lineChart)
        val customColors = intArrayOf(R.color.white, R.color.yellow, R.color.BarBlue, R.color.backgroundBlue)

        // Clear existing data
        hourTime.clear()

        // Create x-axis values (representing time in 15-minute intervals)
        for (i in 1..minOf(emplist!!.tempList.size, maxDataPoints)) {
            hourTime.add(i.toDouble() * 15)  // Assuming each index represents a 15-minute interval
        }

        if (emplist!!.tempList.size > maxDataPoints) {
            emplist!!.tempList = emplist!!.tempList.subList(emplist!!.tempList.size - maxDataPoints, emplist!!.tempList.size)
        }

        // Plotting data
        val entries = mutableListOf<Entry>()

        for (i in hourTime.indices) {
            entries.add(Entry(hourTime[i].toFloat(), emplist!!.tempList[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, "Temp vs Time")
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
    private fun updateGraph() {
        if (emplist != null) {
            graphData() // Initial graphing
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({recreate()}, 5 * 60 * 1000) // 15 minutes in milliseconds
        } else {
            Log.e("SensorDetails", "emplist is null in updateGraph()")
        }
    }
    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.Main) {
            if (emplist != null) {
                binding?.displayName?.text = emplist!!.name
                val temperatureList =SensorInfo.fetchDataFromThingSpeak(emplist!!.channel, emplist!!.field)
                val TSdata = SensorInfo.getMostRecentTemperature(temperatureList.reversed())
                binding?.displayEmail?.text = TSdata.toString()
                graphData()
            }
        }
    }

}
