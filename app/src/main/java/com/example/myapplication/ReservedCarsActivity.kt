package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CarsAdapter
import com.example.myapplication.databinding.ActivityReservedCarsBinding
import com.example.myapplication.models.Car
import com.google.firebase.database.*

class ReservedCarsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservedCarsBinding
    private lateinit var database: DatabaseReference
    private val reservedCarsList = mutableListOf<Car>()
    private lateinit var adapter: CarsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservedCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("cars")

        adapter = CarsAdapter(
            reservedCarsList,
            onDetailsClick = { car -> openCarDetails(car) },
            onBookClick = { /* Ничего не делаем */ },
            onCancelReservationClick = { car -> cancelReservation(car) } // Передаем обработчик
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadReservedCars()
    }

    private fun loadReservedCars() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reservedCarsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    if (car != null && !car.reservedBy.isNullOrEmpty()) {
                        reservedCarsList.add(car)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ReservedCarsActivity,
                    "Ошибка загрузки данных: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun openCarDetails(car: Car) {
        val intent = Intent(this, CarDetailsActivity::class.java)
        intent.putExtra("carId", car.id)
        intent.putExtra("reservedBy", car.reservedBy)
        startActivity(intent)
    }

    private fun cancelReservation(car: Car) {
        val carId = car.id ?: return // Проверяем, что car.id не null

        val carRef = database.child(carId)
        carRef.child("reservedBy").setValue(null) // Убираем бронирование
            .addOnSuccessListener {
                Toast.makeText(this, "Бронирование отменено", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
