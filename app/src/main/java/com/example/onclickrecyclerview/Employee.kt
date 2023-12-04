package com.example.onclickrecyclerview

import java.io.Serializable

// Employee model
data class Employee(
    val id: Int,
    val name: String,
    val address: String,
) : Serializable