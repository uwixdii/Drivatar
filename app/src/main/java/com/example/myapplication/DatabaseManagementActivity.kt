package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CarsAdapter
import com.example.myapplication.databinding.ActivityDatabaseManagementBinding
import com.example.myapplication.models.Car
import com.google.firebase.database.*

class DatabaseManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseManagementBinding
    private lateinit var database: DatabaseReference
    private val carsList = mutableListOf<Car>()
    private lateinit var adapter: CarsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase
        database = FirebaseDatabase.getInstance().getReference("cars")

        // Настройка RecyclerView
        adapter = CarsAdapter(carsList, { car -> deleteCar(car) }, true)
        binding.recyclerViewCars.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCars.adapter = adapter

        // Загрузка данных из Firebase
        loadCarsFromDatabase()

        // Кнопка добавления машины
        binding.btnAddCar.setOnClickListener {
            showAddCarDialog()
        }

        // Кнопка просмотра бронирований
        binding.btnViewBookings.setOnClickListener {
            viewBookedCars()
        }
    }

    private fun loadCarsFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                carsList.clear()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    car?.let { carsList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManagement", "Failed to load cars: ${error.message}")
            }
        })
    }

    private fun showAddCarDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_car, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить машину")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = dialogView.findViewById<EditText>(R.id.etCarName)?.text.toString()
                val year = dialogView.findViewById<EditText>(R.id.etCarYear)?.text.toString()
                val mileage = dialogView.findViewById<EditText>(R.id.etCarMileage)?.text.toString()
                val color = dialogView.findViewById<EditText>(R.id.etCarColor)?.text.toString()
                val price = dialogView.findViewById<EditText>(R.id.etCarPrice)?.text.toString()

                if (name.isNotEmpty() && year.isNotEmpty() && mileage.isNotEmpty() && color.isNotEmpty() && price.isNotEmpty()) {
                    addCar(name, year, mileage, color, price)
                } else {
                    Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun addCar(name: String, year: String, mileage: String, color: String, price: String) {
        val carId = database.push().key ?: return
        val car = Car(carId, name, year, price, null, mileage, color, false)
        database.child(carId).setValue(car).addOnSuccessListener {
            Toast.makeText(this, "Машина добавлена", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCar(car: Car) {
        car.id?.let {
            database.child(it).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Машина удалена", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewBookedCars() {
        val bookedCars = carsList.filter { it.reservedBy != null }
        if (bookedCars.isEmpty()) {
            Toast.makeText(this, "Нет забронированных машин", Toast.LENGTH_SHORT).show()
        } else {
            val bookedCarNames = bookedCars.joinToString("\n") { car ->
                "Машина: ${car.name}, Забронирована пользователем: ${car.reservedBy}"
            }
            AlertDialog.Builder(this)
                .setTitle("Забронированные машины")
                .setMessage(bookedCarNames)
                .setPositiveButton("ОК", null)
                .show()
        }
    }
}
