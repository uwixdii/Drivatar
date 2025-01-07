package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityUserDetailsBinding
import com.example.myapplication.models.User
import com.google.firebase.database.*

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId")
        database = FirebaseDatabase.getInstance().getReference("users")

        if (userId != null) {
            loadUserDetails(userId!!)
        }

        binding.btnAssignAdmin.setOnClickListener {
            userId?.let { id -> assignAdminRole(id) }
        }

        binding.btnDeleteUser.setOnClickListener {
            userId?.let { id -> deleteUser(id) }
        }
    }

    private fun loadUserDetails(userId: String) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.tvUserName.text = user.name
                    binding.tvUserEmail.text = user.email
                    binding.tvUserRole.text = user.role
                } else {
                    Toast.makeText(this@UserDetailsActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserDetailsActivity, "Ошибка загрузки: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun assignAdminRole(userId: String) {
        database.child(userId).child("role").setValue("admin")
            .addOnSuccessListener {
                Toast.makeText(this, "Пользователь назначен администратором!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка назначения: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUser(userId: String) {
        database.child(userId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Пользователь удалён!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка удаления: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
