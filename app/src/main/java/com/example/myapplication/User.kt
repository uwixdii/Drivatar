package com.example.myapplication.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user" // Значение по умолчанию
)
