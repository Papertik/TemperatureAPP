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
import com.example.onclickrecyclerview.Employee
import com.example.onclickrecyclerview.ItemAdapter


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
    private var binding:EmployeeDetailsBinding?=null
    private lateinit var adapter: ItemAdapter
    private lateinit var employeeToDelete:Employee
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
            var TSdata = fetchDataFromThingSpeak(emplist!!.address)
            binding?.displayEmail?.text = TSdata // displaying email
        }

    var initEmployeeList = ArrayList<Employee>()
    adapter = ItemAdapter(initEmployeeList, object : ItemAdapter.OnDeleteClickListener {
         override fun onDeleteClick(employee: Employee) {
            // Set the employeeToDelete before deletion
            employeeToDelete = employee
            val employeeIdToDelete = employee.id
            if (employeeIdToDelete != -1) {
                // Call the delete method in the adapter
                adapter.deleteEmployeeById(employeeIdToDelete)
                finish() // Optionally, finish the activity after deletion
                Toast.makeText(
                    this@EmployeeDetails,
                    "Employee ${employee.name} deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    })
//        val deleteButton = findViewById<Button>(R.id.DeleteButton)
//
//        // Set an OnClickListener for the DeleteButton
//        deleteButton.setOnClickListener {
//            // Call the onDeleteClick method when the DeleteButton is clicked
//            adapter.onDeleteClick(employeeToDelete)
//        }
        }
    }
