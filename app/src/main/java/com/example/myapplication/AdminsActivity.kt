package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAdminsBinding

class AdminsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnViewReservedCars.setOnClickListener {
            val intent = Intent(this, ReservedCarsActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewAvailableCars.setOnClickListener {
            val intent = Intent(this, AvailableCarsActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddCar.setOnClickListener {
            val intent = Intent(this, AddCarActivity::class.java)
            startActivity(intent)
        }

        binding.btnManageUsers.setOnClickListener {
            val intent = Intent(this, ManageUsersActivity::class.java)
            startActivity(intent)
        }
    }
}
