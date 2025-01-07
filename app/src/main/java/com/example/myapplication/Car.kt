package com.example.myapplication.models

data class Car(
    val id: String? = null,
    val name: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    var reservedBy: String? = null,
    val mileage: Int = 0,
    val color: String = "",
    val isAvailable: Boolean = true
)
