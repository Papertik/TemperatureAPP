package com.example.onclickrecyclerview

import android.util.Log

object EmployeeInfo {
    // This Method adds an employee to the existing ArrayList
    fun addEmployeeToDataList(employeeList: ArrayList<Employee>, NAME: String, Address: String) {
        val emp = Employee(NAME, Address)
        employeeList.add(emp)
        Log.d("EmployeeInfo", "Added employee with NAME: $NAME and Address: $Address")
    }
}