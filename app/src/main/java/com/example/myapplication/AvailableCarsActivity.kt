package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CarsAdapter
import com.example.myapplication.databinding.ActivityAvailableCarsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AvailableCarsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvailableCarsBinding
    private lateinit var database: DatabaseReference
    private val availableCarsList = mutableListOf<Car>()
    private lateinit var adapter: CarsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("cars")

        adapter = CarsAdapter(
            availableCarsList,
            onDetailsClick = { car ->
                openCarDetails(car)
            },
            onBookClick = { car ->
                bookCar(car)
            },
            onCancelReservationClick = { /* Ничего не делаем */ }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadAvailableCars()
    }

    private fun loadAvailableCars() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableCarsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    if (car != null && car.reservedBy == null) {
                        availableCarsList.add(car)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AvailableCarsActivity", "Failed to load cars: ${error.message}")
            }
        })
    }

    private fun openCarDetails(car: Car) {
        val intent = Intent(this, CarDetailsActivity::class.java)
        intent.putExtra("carId", car.id)
        startActivity(intent)
    }

    private fun bookCar(car: Car) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val carId = car.id ?: return // Выход, если id машины равен null
        val userId = currentUserId ?: return // Выход, если пользователь не авторизован

        database.child(carId).child("reservedBy").setValue(userId)
            .addOnSuccessListener {
                showToast("Машина успешно забронирована!")
            }
            .addOnFailureListener { error ->
                showToast("Ошибка бронирования: ${error.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
