package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCarDetailsBinding
import com.example.myapplication.models.Car
import com.google.firebase.database.*

class CarDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailsBinding
    private lateinit var database: DatabaseReference
    private var carId: String? = null
    private var reservedBy: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getStringExtra("carId")
        reservedBy = intent.getStringExtra("reservedBy")

        if (carId != null) {
            loadCarDetails(carId!!)
        }

        if (reservedBy != null) {
            loadReservedUserDetails(reservedBy!!)
        } else {
            binding.tvReservedBy.text = "Машина не забронирована"
        }
    }

    private fun loadCarDetails(carId: String) {
        database = FirebaseDatabase.getInstance().getReference("cars").child(carId)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val car = snapshot.getValue(Car::class.java)
                if (car != null) {
                    binding.tvCarName.text = car.name
                    binding.tvCarYear.text = car.year.toString()
                    binding.tvCarPrice.text = "${car.price} USD"
                    binding.tvCarColor.text = car.color
                    binding.tvCarMileage.text = "${car.mileage} km"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CarDetailsActivity, "Ошибка загрузки данных машины", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadReservedUserDetails(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java) ?: "Неизвестно"
                val userEmail = snapshot.child("email").getValue(String::class.java) ?: "Неизвестно"

                binding.tvReservedBy.text = "Имя: $userName\nПочта: $userEmail"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CarDetailsActivity, "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
