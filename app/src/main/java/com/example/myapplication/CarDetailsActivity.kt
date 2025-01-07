package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCarDetailsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CarDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailsBinding
    private lateinit var carId: String
    private val database = FirebaseDatabase.getInstance().getReference("cars")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение ID машины из Intent
        carId = intent.getStringExtra("carId") ?: return

        // Загрузка данных о машине
        loadCarDetails()

        // Настройка кнопки бронирования
        binding.btnReserve.setOnClickListener {
            reserveCar()
        }
    }

    private fun loadCarDetails() {
        database.child(carId).get().addOnSuccessListener { snapshot ->
            val car = snapshot.getValue(Car::class.java)
            if (car != null) {
                binding.tvCarName.text = car.name
                binding.tvCarYear.text = car.year
                binding.tvCarPrice.text = car.price
                binding.tvReservedBy.text = if (car.reservedBy != null) {
                    "Забронирована пользователем: ${car.reservedBy}"
                } else {
                    "Свободна"
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reserveCar() {
        if (userId == null) {
            Toast.makeText(this, "Вы должны быть авторизованы для бронирования", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(carId).child("reservedBy").setValue(userId).addOnSuccessListener {
            Toast.makeText(this, "Машина успешно забронирована", Toast.LENGTH_SHORT).show()
            finish() // Закрываем активность после бронирования
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка бронирования", Toast.LENGTH_SHORT).show()
        }
    }
}
