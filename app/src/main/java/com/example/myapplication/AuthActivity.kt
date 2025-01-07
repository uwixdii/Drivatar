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

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Введите email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Введите пароль"
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user == null) {
                        Toast.makeText(this, "Ошибка: Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val userId = user.uid
                    database.child(userId).child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val role = snapshot.getValue(String::class.java)
                            if (role.isNullOrEmpty()) {
                                Toast.makeText(this@AuthActivity, "Ошибка: Роль пользователя не определена", Toast.LENGTH_SHORT).show()
                                return
                            }

                            if (role == "admin") {
                                startActivity(Intent(this@AuthActivity, AdminsActivity::class.java))
                            } else {
                                startActivity(Intent(this@AuthActivity, CarsActivity::class.java))
                            }
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@AuthActivity, "Ошибка базы данных: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Ошибка авторизации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
