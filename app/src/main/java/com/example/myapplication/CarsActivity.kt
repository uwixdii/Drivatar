package com.example.myapplication

import CarsAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityCarsBinding
import com.example.myapplication.models.Car

class CarsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val carsList = listOf(
            Car("Toyota Camry", "2020", "$50/day"),
            Car("BMW X5", "2022", "$100/day"),
            Car("Tesla Model 3", "2021", "$120/day")
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = CarsAdapter(carsList)
    }
}
