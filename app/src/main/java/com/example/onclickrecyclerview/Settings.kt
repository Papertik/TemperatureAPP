package com.example.onclickrecyclerview.ui.theme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.example.onclickrecyclerview.MainActivity
import com.example.onclickrecyclerview.R

class Settings : AppCompatActivity() {
    private fun startDeviceAddActivity() {
        val ADD_EMPLOYEE_REQUEST = 1
        val intent = Intent(this, DeviceAdd::class.java)
        startActivityForResult(intent, ADD_EMPLOYEE_REQUEST)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val imagebuttonClick = findViewById<ImageButton>(R.id.HomeButton)
        imagebuttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // starting activity for adding device when button clicked
//        val deviceClick = findViewById<Button>(R.id.NewDevice)
//        deviceClick.setOnClickListener{
//            val intent = Intent(this, DeviceAdd::class.java)
//            startActivity(intent)
//        }
        // creating intent for wifi settings
        val WifiClick = findViewById<Button>(R.id.WifiSettings)
        WifiClick.setOnClickListener{
            val intent = Intent(this, WIfiSettings::class.java)
            startActivity(intent)
        }
    }
}