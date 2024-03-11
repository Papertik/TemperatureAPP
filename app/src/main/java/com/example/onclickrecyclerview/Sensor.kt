package com.example.onclickrecyclerview

import java.io.Serializable

// sensor model
data class Sensor(
    val id: Int,
    val name: String,
    val channel: String,
    val field: String,
    var tempList: List<Double> = mutableListOf(),
    var temperature: Double =0.0,
):Serializable
//data class TSData(val name: String, val value: Double) :Serializable

