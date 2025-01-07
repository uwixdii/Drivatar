package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityEditCarBinding
import com.google.firebase.database.FirebaseDatabase
import com.example.myapplication.models.Car

class EditCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCarBinding
    private lateinit var carId: String
    private val database = FirebaseDatabase.getInstance().getReference("cars")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getStringExtra("carId") ?: return
        loadCarDetails()

        binding.btnSaveChanges.setOnClickListener { saveCarDetails() }
    }

    private fun loadCarDetails() {
        database.child(carId).get().addOnSuccessListener { snapshot ->
            val car = snapshot.getValue(Car::class.java)
            if (car != null) {
                binding.etCarName.setText(car.name)
                binding.etCarYear.setText(car.year)
                binding.etCarPrice.setText(car.price)
            } else {
                Toast.makeText(this, "Машина не найдена", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка загрузки данных машины", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCarDetails() {
        val newName = binding.etCarName.text.toString().trim()
        val newYear = binding.etCarYear.text.toString().trim()
        val newPrice = binding.etCarPrice.text.toString().trim()

        if (newName.isEmpty() || newYear.isEmpty() || newPrice.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "name" to newName,
            "year" to newYear,
            "price" to newPrice
        )

        database.child(carId).updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Данные машины успешно обновлены", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show()
        }
    }
}
