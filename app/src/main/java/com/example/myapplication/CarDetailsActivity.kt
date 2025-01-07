package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCarDetailsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CarDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailsBinding
    private lateinit var carId: String
    private lateinit var currentCar: Car
    private val database = FirebaseDatabase.getInstance().getReference("cars")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getStringExtra("carId") ?: return

        checkUserRole { admin ->
            isAdmin = admin
            loadCarDetails()
        }

        setupActions()
    }

    private fun loadCarDetails() {
        database.child(carId).get().addOnSuccessListener { snapshot ->
            val car = snapshot.getValue(Car::class.java)
            if (car != null) {
                currentCar = car
                binding.tvCarName.text = car.name
                binding.tvCarYear.text = car.year
                binding.tvCarPrice.text = car.price

                if (!car.reservedBy.isNullOrEmpty()) {
                    loadReservedByDetails(car.reservedBy!!)
                } else {
                    binding.tvReservedBy.text = "Машина свободна"
                }

                setupVisibility(car)
            } else {
                Toast.makeText(this, "Машина не найдена", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка загрузки данных машины", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadReservedByDetails(reservedBy: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(reservedBy)
        userRef.get().addOnSuccessListener { userSnapshot ->
            val name = userSnapshot.child("name").getValue(String::class.java) ?: "Неизвестно"
            val email = userSnapshot.child("email").getValue(String::class.java) ?: "Неизвестно"
            binding.tvReservedBy.text = "Забронирована пользователем: $name ($email)"
        }.addOnFailureListener {
            binding.tvReservedBy.text = "Ошибка загрузки данных пользователя"
        }
    }

    private fun setupVisibility(car: Car) {
        if (isAdmin) {
            binding.btnAdminActions.visibility = View.VISIBLE
            binding.btnReserve.visibility = View.GONE
            binding.btnCancelReservation.visibility = View.VISIBLE // Администратор может отменить чужую бронь
        } else {
            binding.btnAdminActions.visibility = View.GONE
            binding.btnReserve.visibility = if (car.reservedBy.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.btnCancelReservation.visibility = if (car.reservedBy == userId) View.VISIBLE else View.GONE
        }
    }

    private fun setupActions() {
        binding.btnReserve.setOnClickListener { reserveCar() }
        binding.btnCancelReservation.setOnClickListener { cancelReservation() }
        binding.btnAdminActions.setOnClickListener { openEditCarActivity() }
    }

    private fun reserveCar() {
        if (userId == null) {
            Toast.makeText(this, "Вы должны быть авторизованы для бронирования", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(carId).child("reservedBy").setValue(userId).addOnSuccessListener {
            Toast.makeText(this, "Машина успешно забронирована", Toast.LENGTH_SHORT).show()
            loadCarDetails()
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка бронирования", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelReservation() {
        database.child(carId).child("reservedBy").removeValue().addOnSuccessListener {
            Toast.makeText(this, "Бронирование отменено", Toast.LENGTH_SHORT).show()
            loadCarDetails()
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка отмены бронирования", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEditCarActivity() {
        val intent = Intent(this, EditCarActivity::class.java)
        intent.putExtra("carId", carId)
        startActivity(intent)
    }

    private fun checkUserRole(callback: (Boolean) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId ?: return)
        userRef.child("role").get().addOnSuccessListener { snapshot ->
            val role = snapshot.getValue(String::class.java)
            callback(role == "admin")
        }.addOnFailureListener {
            callback(false)
        }
    }
}
