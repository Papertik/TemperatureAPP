package com.example.onclickrecyclerview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
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

    private var binding: EmployeeDetailsBinding? = null
    private lateinit var adapter: ItemAdapter
    private lateinit var employeeToDelete: Employee

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

        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
        imagebutton1Click.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        var emplist: Employee? = null

        if (intent.hasExtra(MainActivity.NEXT_SCREEN)) {
            emplist = intent.getSerializableExtra(MainActivity.NEXT_SCREEN) as Employee
        }

        if (emplist != null) {
            binding?.displayName?.text = emplist!!.name
            var TSdata = fetchDataFromThingSpeak(emplist!!.address)
            binding?.displayEmail?.text = TSdata
        }
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
}
