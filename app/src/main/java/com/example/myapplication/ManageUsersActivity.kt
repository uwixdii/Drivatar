package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.UsersAdapter
import com.example.myapplication.databinding.ActivityManageUsersBinding
import com.example.myapplication.models.User
import com.google.firebase.database.*

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUsersBinding
    private val userList = mutableListOf<User>()
    private lateinit var adapter: UsersAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("users")

        // Настройка адаптера с обработчиками кликов
        adapter = UsersAdapter(userList,
            onUserClick = { user -> openUserDetails(user) },
            onAdminAssignClick = { user -> assignAdminRole(user.id) },
            onDeleteClick = { user -> deleteUser(user.id) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user.copy(id = userSnapshot.key ?: ""))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageUsersActivity, "Ошибка загрузки данных: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openUserDetails(user: User) {
        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra("userId", user.id)
        startActivity(intent)
    }

    private fun assignAdminRole(userId: String) {
        database.child(userId).child("role").setValue("admin")
            .addOnSuccessListener {
                Toast.makeText(this, "Пользователь назначен администратором!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUser(userId: String) {
        database.child(userId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Пользователь удалён из базы данных!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Ошибка удаления: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
