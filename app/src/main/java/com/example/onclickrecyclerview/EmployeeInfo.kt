package com.example.onclickrecyclerview

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object EmployeeInfo {
    private val employeeList = ArrayList<Employee>()
    private var nextId = 1
    // This Method adds an employee to the existing ArrayList
    fun addEmployeeToDataList(name: String, Channel: String, Field: String, Temperature: Double):Employee {
        val employee = Employee(nextId++, name, Channel, Field, Temperature)
        employeeList.add(employee)
        Log.d("EmployeeInfo", "Added employee with NAME: $name and Channel: $Channel, Field: $Field")
        return employee
    }
    fun getEmployeeData(): ArrayList<Employee> {
        return employeeList
    }

    // Add a function to delete an employee
    fun deleteEmployee(id: Int) {
        employeeList.removeAll { it.id == id }
    }
    fun writeEmployeesToCsv(file: File, employees: List<Employee>) {
        BufferedWriter(FileWriter(file)).use { writer ->


            // Write each employee's data
            employees.forEach { employee ->
                writer.write("${employee.id},${employee.name},${employee.channel},${employee.field},${employee.temperature}\n")
            }
        }
    }
}