package com.example.onclickrecyclerview

import android.util.Log

object EmployeeInfo {
    private val employeeList = ArrayList<Employee>()
    private var nextId = 1
    // This Method adds an employee to the existing ArrayList
    fun addEmployeeToDataList(name: String, address: String):Employee {
        val employee = Employee(nextId++, name, address)
        employeeList.add(employee)
        Log.d("EmployeeInfo", "Added employee with NAME: $name and Address: $address")
        return employee
    }
    fun getEmployeeData(): ArrayList<Employee> {
        return employeeList
    }

    // Add a function to delete an employee
    fun deleteEmployee(id: Int) {
        employeeList.removeAll { it.id == id }
    }
}