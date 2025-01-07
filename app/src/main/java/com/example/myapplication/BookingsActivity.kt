package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CarsAdapter
import com.example.myapplication.databinding.ActivityBookingsBinding
import com.example.myapplication.models.Car
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingsBinding
    private val database = FirebaseDatabase.getInstance().getReference("cars")
    private val bookedCarsList = mutableListOf<Car>()
    private lateinit var adapter: CarsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView
        adapter = CarsAdapter(bookedCarsList, { car ->
            Toast.makeText(this, "Забронирована: ${car.name}", Toast.LENGTH_SHORT).show()
        }, isAdmin = true)  // передаем true, если пользователь является администратором

        binding.recyclerViewBookings.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBookings.adapter = adapter

        // Загрузка данных о бронированиях
        loadBookings()
    }

    private fun loadBookings() {
        // Используем addValueEventListener для обновлений в реальном времени
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookedCarsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    if (car != null && car.reservedBy != null) {
                        // Получаем UID пользователя, который забронировал машину
                        val reservedByUid = car.reservedBy

                        // Проверяем, что reservedByUid не null
                        if (!reservedByUid.isNullOrEmpty()) {
                            // Загружаем информацию о пользователе по UID
                            val userRef = FirebaseDatabase.getInstance().getReference("users").child(reservedByUid)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    // Проверка, что данные пользователя загружены
                                    val userName = userSnapshot.child("name").getValue(String::class.java)
                                    val userEmail = userSnapshot.child("email").getValue(String::class.java)

                                    // Обновляем поле reservedBy с именем и почтой
                                    car.reservedBy = "$userName ($userEmail)"

                                    // Добавляем машину в список забронированных
                                    bookedCarsList.add(car)
                                    adapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("BookingsActivity", "Ошибка загрузки данных пользователя: ${error.message}")
                                }
                            })
                        } else {
                            Log.e("BookingsActivity", "Ошибка: UID забронировавшего пользователя пустое")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookingsActivity", "Ошибка загрузки бронирований: ${error.message}")
            }
        })
    }
}
