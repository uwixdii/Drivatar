package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCarDetailsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CarDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailsBinding
    private lateinit var database: DatabaseReference
    private var carId: String? = null
    private var reservedBy: String? = null
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getStringExtra("carId")
        reservedBy = intent.getStringExtra("reservedBy")

        // Определяем, является ли текущий пользователь администратором
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        isAdmin = currentUserId == "adminUserId" // Укажите реальный ID администратора

        if (carId != null) {
            loadCarDetails(carId!!)
        }

        if (reservedBy != null) {
            loadReservedUserDetails(reservedBy!!)
        } else {
            binding.tvReservedBy.text = "Машина не забронирована"
        }

        setupButtons()
    }

    private fun setupButtons() {
        // Кнопка "Скрыть машину" и "Удалить машину" только для администратора
        if (isAdmin) {
            binding.btnHideCar.visibility = View.VISIBLE
            binding.btnDeleteCar.visibility = View.VISIBLE
        }

        // Кнопка "Отменить бронирование" видна, если машина забронирована
        if (reservedBy != null) {
            binding.btnCancelReservation.visibility = View.VISIBLE
        }

        // Обработчики кнопок
        binding.btnCancelReservation.setOnClickListener {
            cancelReservation()
        }

        binding.btnHideCar.setOnClickListener {
            hideCar()
        }

        binding.btnDeleteCar.setOnClickListener {
            deleteCar()
        }
    }

    private fun cancelReservation() {
        if (carId == null) return

        database = FirebaseDatabase.getInstance().getReference("cars").child(carId!!)
        database.child("reservedBy").setValue(null)
            .addOnSuccessListener {
                Toast.makeText(this, "Бронирование отменено", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideCar() {
        if (carId == null) return

        database = FirebaseDatabase.getInstance().getReference("cars").child(carId!!)
        database.child("isHidden").setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Машина скрыта", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCar() {
        if (carId == null) return

        database = FirebaseDatabase.getInstance().getReference("cars").child(carId!!)
        database.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Машина удалена", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
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
