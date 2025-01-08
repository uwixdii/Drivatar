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
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("cars")

        // Определяем, является ли текущий пользователь администратором
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        isAdmin = currentUserId == "adminUserId" // Укажите реальный ID администратора

        // Инициализируем адаптер
        adapter = CarsAdapter(
            carsList = availableCarsList,
            isAdmin = isAdmin,
            onDetailsClick = { car -> openCarDetails(car) },
            onBookClick = { car -> bookCar(car) },
            onCancelReservationClick = { car -> cancelReservation(car) },
            onHideClick = { car -> hideCar(car) },
            onDeleteClick = { car -> deleteCar(car) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Загружаем доступные автомобили
        loadAvailableCars()
    }

    private fun loadAvailableCars() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableCarsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    // Исключаем машины, которые забронированы
                    if (car != null && car.isHidden != true && car.reservedBy == null) {
                        availableCarsList.add(car)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AvailableCarsActivity", "Ошибка загрузки машин: ${error.message}")
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
        val carId = car.id ?: return
        val userId = currentUserId ?: return

        database.child(carId).child("reservedBy").setValue(userId)
            .addOnSuccessListener {
                showToast("Машина успешно забронирована!")
                loadAvailableCars() // Обновляем список машин
            }
            .addOnFailureListener { error ->
                showToast("Ошибка бронирования: ${error.message}")
            }
    }


    private fun cancelReservation(car: Car) {
        val carId = car.id ?: return

        database.child(carId).child("reservedBy").setValue(null)
            .addOnSuccessListener {
                showToast("Бронирование отменено!")
                loadAvailableCars() // Обновляем список
            }
            .addOnFailureListener { error ->
                showToast("Ошибка отмены бронирования: ${error.message}")
            }
    }

    private fun hideCar(car: Car) {
        val carId = car.id ?: return

        database.child(carId).child("isHidden").setValue(true)
            .addOnSuccessListener {
                showToast("Машина успешно скрыта!")
                loadAvailableCars() // Обновляем список
            }
            .addOnFailureListener { error ->
                showToast("Ошибка скрытия машины: ${error.message}")
            }
    }

    private fun deleteCar(car: Car) {
        val carId = car.id ?: return

        database.child(carId).removeValue()
            .addOnSuccessListener {
                showToast("Машина успешно удалена!")
                loadAvailableCars() // Обновляем список
            }
            .addOnFailureListener { error ->
                showToast("Ошибка удаления машины: ${error.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
