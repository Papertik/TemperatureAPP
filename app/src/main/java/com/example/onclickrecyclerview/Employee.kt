package com.example.onclickrecyclerview

import java.io.Serializable

// Employee model
data class Employee(
    val id: Int,
    val name: String,
    val channel: String,
    val field: String,
) : Serializable
//data class TSData(val name: String, val value: Double) :Serializable

