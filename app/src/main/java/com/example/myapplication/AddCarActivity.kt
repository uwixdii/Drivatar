package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddCarBinding
import com.example.myapplication.models.Car
import com.google.firebase.database.FirebaseDatabase

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddCar.setOnClickListener {
            val carName = binding.etCarName.text.toString().trim()
            val carYearText = binding.etCarYear.text.toString().trim()
            val carPriceText = binding.etCarPrice.text.toString().trim()
            val carColor = binding.etCarColor.text.toString().trim()
            val carMileageText = binding.etCarMileage.text.toString().trim()

            // Проверка пустых полей
            if (carName.isEmpty()) {
                binding.etCarName.error = "Введите название машины"
                return@setOnClickListener
            }
            if (carYearText.isEmpty()) {
                binding.etCarYear.error = "Введите год выпуска"
                return@setOnClickListener
            }
            if (carPriceText.isEmpty()) {
                binding.etCarPrice.error = "Введите цену"
                return@setOnClickListener
            }
            if (carColor.isEmpty()) {
                binding.etCarColor.error = "Введите цвет машины"
                return@setOnClickListener
            }
            if (carMileageText.isEmpty()) {
                binding.etCarMileage.error = "Введите пробег"
                return@setOnClickListener
            }

            // Проверка корректности данных
            val carYear = carYearText.toIntOrNull()
            val carPrice = carPriceText.toDoubleOrNull()
            val carMileage = carMileageText.toIntOrNull()

            if (carYear == null || carYear < 1886 || carYear > 2025) { // Пределы для года
                binding.etCarYear.error = "Введите корректный год (1886 - 2025)"
                return@setOnClickListener
            }

            if (carPrice == null || carPrice <= 0) {
                binding.etCarPrice.error = "Введите корректную цену (больше 0)"
                return@setOnClickListener
            }

            if (carMileage == null || carMileage < 0) {
                binding.etCarMileage.error = "Введите корректный пробег (0 или больше)"
                return@setOnClickListener
            }

            // Добавление машины в базу данных
            val carId = FirebaseDatabase.getInstance().getReference("cars").push().key ?: ""
            val car = Car(
                id = carId,
                name = carName,
                year = carYear,
                price = carPrice,
                color = carColor,
                mileage = carMileage,
                reservedBy = null
            )

            FirebaseDatabase.getInstance().getReference("cars")
                .child(carId)
                .setValue(car)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Машина успешно добавлена!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Ошибка добавления: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
