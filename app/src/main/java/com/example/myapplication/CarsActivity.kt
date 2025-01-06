package com.example.myapplication

import CarsAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityCarsBinding
import com.example.myapplication.models.Car
import com.google.firebase.auth.FirebaseAuth

class CarsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Настройка списка машин
        val carsList = listOf(
            Car("Toyota Camry", "2020", "$50/day"), // 1
            Car("BMW X5", "2022", "$100/day"),
            Car("Tesla Model 3", "2021", "$120/day")
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = CarsAdapter(carsList)

        // Настройка кнопки "Выход"
        val logoutButton: Button = findViewById(R.id.btnLogout)
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        auth.signOut() // Firebase: Выход из аккаунта
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Удаляем историю стека
        startActivity(intent)
        finish()
    }
}
