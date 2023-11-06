package com.example.onclickrecyclerview

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onclickrecyclerview.databinding.MainActivityBinding
import com.example.onclickrecyclerview.ui.theme.DeviceAdd
import com.example.onclickrecyclerview.ui.theme.Settings

// In this project we are going to use view binding
class MainActivity : AppCompatActivity() {

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val NAME = data?.getStringExtra("NAME") ?: "No name provided"
                val Address = data?.getStringExtra("Address") ?: "No value Provided"
                val newEmployee = EmployeeInfo.addEmployeeToDataList(EmployeeList, NAME, Address)
                (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(EmployeeList)
            }
        }

    // View Binding
    var binding:MainActivityBinding?=null
    private var EmployeeList: ArrayList<Employee> = ArrayList()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called")

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val NAME = data?.getStringExtra("NAME") ?: "No name provided"
            val Address = data?.getStringExtra("Address") ?: "No value Provided"

            // Assuming employeeList is a class variable
            val newEmployee = EmployeeInfo.addEmployeeToDataList(EmployeeList,NAME, Address)

            // Update RecyclerView adapter with the new data
            (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(EmployeeList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // settings button to go to settings
        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
        imagebutton1Click.setOnClickListener {
            val intentSettings = Intent(this, Settings::class.java)
            startActivity(intentSettings)
        }


        // getting the employee list by
        // calling getEmployeeData method
        val intent = intent

        binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
        binding?.rvItemsList?.setHasFixedSize(true)

        // Creating an instance of the
        // adapter and passing emplist to it
        val ItemAdapter = ItemAdapter(EmployeeList)

        // Assign ItemAdapter instance to our RecylerView
        binding?.rvItemsList?.adapter = ItemAdapter

        // Applying OnClickListener to our Adapter
        ItemAdapter.setOnClickListener(object :
            ItemAdapter.OnClickListener {
            override fun onClick(position: Int, model: Employee) {
                val intent = Intent(this@MainActivity, EmployeeDetails::class.java)
                // Passing the data to the
                // EmployeeDetails Activity
                intent.putExtra(NEXT_SCREEN, model)
                startActivity(intent)
            }
        })
        binding?.addbutton?.setOnClickListener {
            startDeviceAddActivity()
        }
    }

    private fun startDeviceAddActivity() {
        val intent = Intent(this, DeviceAdd::class.java)
        startForResult.launch(intent)
    }
    companion object{
        val NEXT_SCREEN="details_screen"
    }

}