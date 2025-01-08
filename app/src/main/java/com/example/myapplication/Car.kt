package com.example.myapplication.models

data class Car(
    val id: String? = null,
    val name: String? = null,
    val year: Int? = null,
    val price: Double? = null,
    val color: String? = null,
    val mileage: Int? = null,
    val reservedBy: String? = null,
    val isHidden: Boolean? = false,
    var isDetailsVisible: Boolean = false
)

