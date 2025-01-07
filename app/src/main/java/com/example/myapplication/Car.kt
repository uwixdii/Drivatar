package com.example.myapplication.models

data class Car(
    val id: String? = null,
    val name: String = "",
    val year: String = "",
    val price: String = "",
    var reservedBy: String? = null,
    val mileage: String = "",
    val color: String = "",
    val isAvailable: Boolean = true
)
