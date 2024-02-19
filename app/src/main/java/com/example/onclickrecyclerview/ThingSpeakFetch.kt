package com.example.onclickrecyclerview

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow

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
fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return kotlin.math.round(this * multiplier) / multiplier
}
