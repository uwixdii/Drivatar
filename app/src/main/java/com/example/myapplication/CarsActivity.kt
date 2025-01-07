package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private var isAdmin: Boolean = false  // Переменная для хранения роли пользователя

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("cars")

        // Проверка роли пользователя
        val userId = auth.currentUser?.uid
        val adminDatabase = FirebaseDatabase.getInstance().getReference("admins")

        adminDatabase.child(userId ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isAdmin = snapshot.exists()  // Сохраняем роль пользователя
                    if (isAdmin) {
                        // Пользователь является администратором
                        binding.btnManageDatabase.visibility = View.VISIBLE
                        binding.btnManageDatabase.setOnClickListener {
                            openDatabaseManagement()
                        }
                    }
                    // Загрузка машин после проверки роли
                    loadCarsFromDatabase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CarsActivity", "Failed to check admin role: ${error.message}")
                }
            })

        // Настройка RecyclerView
        adapter = CarsAdapter(carsList, { car ->
            openCarDetails(car)  // Функция, открывающая детали машины
        }, isAdmin)  // Передача информации о том, является ли пользователь администратором

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Настройка кнопки "Выход"
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadCarsFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                carsList.clear()

                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    if (car != null) {
                        if (isAdmin) {
                            // Администратор видит все машины
                            carsList.add(car)
                        } else {
                            // Обычный пользователь видит только не забронированные машины
                            if (car.reservedBy == null) {
                                carsList.add(car)
                            }
                        }
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

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun openDatabaseManagement() {
        val intent = Intent(this, DatabaseManagementActivity::class.java)
        startActivity(intent)
    }
}
