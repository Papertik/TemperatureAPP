package com.example.onclickrecyclerview.ui.theme

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.example.onclickrecyclerview.EmployeeInfo
import com.example.onclickrecyclerview.MainActivity
import com.example.onclickrecyclerview.R

class DeviceAdd : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_add)


        val imagebuttonClick = findViewById<ImageButton>(R.id.HomeButton)
        imagebuttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // settings button to go to settings
        val imagebutton1Click = findViewById<ImageButton>(R.id.SettingsButton)
        imagebutton1Click.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val addDevice = findViewById<Button>(R.id.NewDevice)
        addDevice.setOnClickListener {
            Log.d("DeviceAdd", "Add Device Button Clicked")
            val deviceName = findViewById<EditText>(R.id.Name).text.toString()
            val TSchannel = findViewById<EditText>(R.id.ChannelNUM).text.toString()
            val TSField = findViewById<EditText>(R.id.Field).text.toString()

            // Check if the deviceName already exists in the EmployeeInfo
            val existingEmployee = EmployeeInfo.getEmployeeData().find { it.name == deviceName }

            if (existingEmployee != null) {
                // Device with the same name already exists, show an error message or handle it accordingly
                Log.d("DeviceAdd", "Device with the same name already exists: $deviceName")
                Toast.makeText(this, "Device with the same name already exists", Toast.LENGTH_SHORT).show()
            } else {
                //Passing data to homescreen
                val intent = Intent()
                intent.putExtra("NAME", deviceName)
                intent.putExtra("Channel", TSchannel)
                intent.putExtra("Field", TSField)
                setResult(Activity.RESULT_OK, intent)
                finish()
                Log.d("DeviceAdd", "Device Name: $deviceName, TScreds: $TSchannel, $TSField")
            }
        }
    }
}