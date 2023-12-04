package com.example.onclickrecyclerview

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onclickrecyclerview.EmployeeInfo.addEmployeeToDataList
import com.example.onclickrecyclerview.databinding.MainActivityBinding
import com.example.onclickrecyclerview.ui.theme.DeviceAdd
import com.example.onclickrecyclerview.ui.theme.Settings

// In this project we are going to use view binding
class MainActivity : AppCompatActivity(), ItemAdapter.OnDeleteClickListener {

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val NAME = data?.getStringExtra("NAME") ?: "No name provided"
                val Address = data?.getStringExtra("Address") ?: "No value Provided"

                val newEmployee = addEmployeeToDataList(NAME, Address)
                EmployeeList.add(newEmployee)
                (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(EmployeeList)
            }
        }

    // View Binding


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called")

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val NAME = data?.getStringExtra("NAME") ?: "No name provided"
            val Address = data?.getStringExtra("Address") ?: "No value Provided"


            // Update RecyclerView adapter with the new data
            (binding?.rvItemsList?.adapter as? ItemAdapter)?.updateData(EmployeeList)
        }
    }
    var binding:MainActivityBinding?=null
    private var EmployeeList: ArrayList<Employee> = ArrayList()
    private lateinit var adapter: ItemAdapter
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
        adapter = ItemAdapter(EmployeeInfo.getEmployeeData(), object : ItemAdapter.OnDeleteClickListener {
            override fun onDeleteClick(employee: Employee) {
                // Handle delete click here
                EmployeeInfo.deleteEmployee(employee.id)
                adapter.notifyDataSetChanged()
            }
        })

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
            }
        })
        binding?.addbutton?.setOnClickListener {
            startDeviceAddActivity()
        }
    }
    // deleet shtuff
    override fun onDeleteClick(employee: Employee) {
        // Handle the delete action here
        // You can show a toast message if needed
        val position = EmployeeList.indexOf(employee)
        if (position != -1) {
            EmployeeList.removeAt(position)
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    companion object{
        val NEXT_SCREEN="details_screen"
    }
}