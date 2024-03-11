package com.example.onclickrecyclerview

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow

object SensorInfo {
    private val sensorList = ArrayList<Sensor>()
    private var nextId = 1
    // This Method adds an sensor to the existing ArrayList
//    fun addsensorToDataList(
//        name: String, Channel: String, Field: String,
//        ListTemp: List<Double>, Temperature: Double):sensor {
//        val sensor = sensor(nextId++, name, Channel, Field, ListTemp, Temperature)
//        sensorList.add(sensor)
//        Log.d("sensorInfo", "Added sensor with NAME: $name and Channel: $Channel, Field: $Field")
//        return sensor
//    }
    fun getsensorData(): ArrayList<Sensor> {
        return sensorList
    }

    // Add a function to delete an sensor
    fun deletesensor(id: Int) {
        sensorList.removeAll { it.id == id }
    }
    fun writesensorsToCsv(file: File, sensors: List<Sensor>) {
        BufferedWriter(FileWriter(file)).use { writer ->

// todo for graph persistance, save the "daydata" list from sensordetails activity, gonna need to make it part of the class.
            // Write each sensor's data
            sensors.forEach { sensor ->
                writer.write("${sensor.id},${sensor.name},${sensor.channel},${sensor.field},${sensor.tempList.reversed()}\n")
            }
        }
    }

suspend fun fetchDataFromThingSpeak(ChannelID: String?, fieldnum: String?): List<Double> {
        return withContext(Dispatchers.IO) {
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

                    val temperatureList = mutableListOf<Double>()

                    for (i in feeds.length() - 1 downTo 0) {
                        val currentFeed = feeds.getJSONObject(i)
                        val fieldValue = currentFeed.optString("field$fieldnum")
                        Log.d("ThingSpeak Addy", url.toString())

                        // Check if the field is not null
                        if (fieldValue.isNotEmpty()) {
                            try {
                                val temperature = fieldValue.toDouble()
                                temperatureList.add(temperature.round(1))
                            } catch (e: NumberFormatException) {
                                // Handle conversion error if needed
                            }
                        }

                        // Break the loop if we have collected 24 temperatures
                        if (temperatureList.size >= 24) {
                            break
                        }
                    }

                    // Reverse the list so that the most recent values are at the beginning
                    temperatureList.reverse()

                    return@withContext temperatureList
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("FetchData", "Exception: ${e.message}")
                    return@withContext emptyList() // Return an empty list for network issues or JSON parsing issues
                }
            } else {
                return@withContext emptyList() // Return an empty list for empty input fields
            }
        }
    }
    fun getMostRecentTemperature(temperatureList: List<Double>): Double {
        return temperatureList.firstOrNull() ?: 0.4 // Return 0.1 if the list is empty
    }

    fun Double.round(decimals: Int): Double {
        val multiplier = 10.0.pow(decimals)
        return kotlin.math.round(this * multiplier) / multiplier
    }
}