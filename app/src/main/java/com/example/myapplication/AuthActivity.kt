package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация ViewBinding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Обработчик кнопки входа
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Пожалуйста, заполните оба поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    // Проверяем роль пользователя в базе данных
                    database.child(userId).child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val role = snapshot.getValue(String::class.java)

                            // Проверяем, является ли пользователь администратором
                            if (role == "admin") {
                                Toast.makeText(this@AuthActivity, "Вы вошли как администратор", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@AuthActivity, CarsActivity::class.java)
                                intent.putExtra("isAdmin", true) // Передаём флаг в CarsActivity
                                startActivity(intent)
                            } else {
                                // Получаем имя пользователя из базы данных
                                database.child(userId).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val name = snapshot.getValue(String::class.java) ?: "Пользователь"
                                        Toast.makeText(this@AuthActivity, "Добро пожаловать, $name", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this@AuthActivity, CarsActivity::class.java)
                                        intent.putExtra("isAdmin", false) // Передаём флаг в CarsActivity
                                        startActivity(intent)
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@AuthActivity, "Ошибка получения данных пользователя: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@AuthActivity, "Ошибка получения данных пользователя: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Ошибка авторизации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
