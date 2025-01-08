package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CarsAdapter
import com.example.myapplication.databinding.ActivityCarsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CarsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val carsList = mutableListOf<Car>()
    private lateinit var adapter: CarsAdapter
    private var isAdmin: Boolean = false // Флаг для проверки администратора

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("cars")

        // Определяем, является ли текущий пользователь администратором
        val currentUserId = auth.currentUser?.uid
        isAdmin = currentUserId == "adminUserId" // Замените "adminUserId" на реальный ID администратора

        // Инициализируем адаптер с дополнительными параметрами
        adapter = CarsAdapter(
            carsList,
            isAdmin = isAdmin, // Передаем флаг для проверки администратора
            onDetailsClick = { car -> openCarDetails(car) },
            onBookClick = { car -> bookCar(car) },
            onCancelReservationClick = { /* Ничего не делаем для обычных пользователей */ },
            onHideClick = { car -> hideCar(car) }, // Обработчик для скрытия машины
            onDeleteClick = { car -> deleteCar(car) } // Обработчик для удаления машины
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnLogout.setOnClickListener {
            logout()
        }

        loadCarsFromDatabase()
    }

    private fun loadCarsFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                carsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    if (car != null && car.reservedBy == null) {
                        carsList.add(car)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CarsActivity", "Failed to load cars: ${error.message}")
            }
        })
    }

    private fun openCarDetails(car: Car) {
        val intent = Intent(this, CarDetailsActivity::class.java)
        intent.putExtra("carId", car.id)
        startActivity(intent)
    }

    private fun bookCar(car: Car) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            car.id?.let { carId ->
                database.child(carId).child("reservedBy").setValue(currentUserId)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Машина успешно забронирована!", Toast.LENGTH_SHORT).show()
                        loadCarsFromDatabase() // Обновить список
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Ошибка бронирования: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(this, "ID машины отсутствует!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Не удалось получить данные пользователя", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideCar(car: Car) {
        val carId = car.id ?: return

        // Если это администратор, скрыть машину
        if (isAdmin) {
            database.child(carId).child("isHidden").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Машина скрыта!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка скрытия: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteCar(car: Car) {
        val carId = car.id ?: return

        // Если это администратор, удалить машину
        if (isAdmin) {
            database.child(carId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Машина удалена!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка удаления: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
