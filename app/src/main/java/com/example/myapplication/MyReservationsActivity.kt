package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CarsAdapter
import com.example.myapplication.databinding.ActivityMyReservationsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyReservationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReservationsBinding
    private lateinit var database: DatabaseReference
    private val reservedCarsList = mutableListOf<Car>()
    private lateinit var adapter: CarsAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference("cars")

        adapter = CarsAdapter(
            carsList = reservedCarsList,
            isAdmin = false, // Для MyReservationsActivity предполагаем, что пользователь не администратор
            onDetailsClick = { car -> openCarDetails(car) },
            onBookClick = { /* Ничего не делаем */ },
            onCancelReservationClick = { car -> cancelReservation(car) },
            onHideClick = { /* Ничего не делаем */ },
            onDeleteClick = { /* Ничего не делаем */ }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadReservedCars(currentUserId)
    }

    private fun loadReservedCars(userId: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reservedCarsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    if (car != null && car.reservedBy == userId) {
                        reservedCarsList.add(car)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyReservationsActivity, "Ошибка загрузки данных: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openCarDetails(car: Car) {
        Toast.makeText(this, "Детали машины: ${car.name}", Toast.LENGTH_SHORT).show()
    }

    private fun cancelReservation(car: Car) {
        val carId = car.id ?: return
        database.child(carId).child("reservedBy").setValue(null)
            .addOnSuccessListener {
                Toast.makeText(this, "Бронирование отменено!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
