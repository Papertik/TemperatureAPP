package com.example.onclickrecyclerview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.onclickrecyclerview.databinding.EmployeeDetailsBinding
import com.example.onclickrecyclerview.ui.theme.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class EmployeeDetails : AppCompatActivity() {

    private fun fetchDataFromThingSpeak(address: String?): String {
        return runBlocking {
            withContext(Dispatchers.IO) {
                if (!address.isNullOrEmpty()) {
                    try {
                        val url = URL(address)
                        val connection = url.openConnection() as HttpURLConnection

                        val inputStream = connection.inputStream
                        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                        val jsonResponse = bufferedReader.readText()

                        val jsonObject = JSONObject(jsonResponse)
                        val feeds = jsonObject.getJSONArray("feeds")
                        if (feeds.length() > 0) {
                            val firstFeed = feeds.getJSONObject(0)
                            return@withContext firstFeed.getString("field1")
                        } else {
                            return@withContext "No data available"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@withContext "Error fetching data: ${e.message}"
                    }
                } else {
                    return@withContext "Empty address"
                }
            }
        }
    }
    var binding:EmployeeDetailsBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EmployeeDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        val imagebuttonClick = findViewById<ImageButton>(R.id.HomeButton)
        imagebuttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // settings button to go to settings
        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
        imagebutton1Click.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)}

        // creating an employee list
        // of type Employee class
        var emplist: Employee? = null
        // checking if the intent has extra
        if (intent.hasExtra(MainActivity.NEXT_SCREEN)) {
            // get the Serializable data model class with the details in it
            emplist = intent.getSerializableExtra(MainActivity.NEXT_SCREEN) as Employee
        }
        // it the emplist is not null the it has some data and display it
        if (emplist != null) {
            binding?.displayName?.text = emplist!!.name// displaying name
            var TSdata = fetchDataFromThingSpeak(emplist!!.email)
            binding?.displayEmail?.text = TSdata // displaying email
        }
    }
}