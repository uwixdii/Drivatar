package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCarBinding
import com.example.myapplication.models.Car

class CarsAdapter(
    private val cars: List<Car>,
    private val onItemClick: (Car) -> Unit,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<CarsAdapter.CarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.bind(car)
    }

    override fun getItemCount(): Int = cars.size

    inner class CarViewHolder(private val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(car: Car) {
            binding.tvCarName.text = car.name
            binding.tvCarYear.text = car.year
            binding.tvCarPrice.text = car.price

            if (isAdmin) {
                binding.tvReservedBy.text = car.reservedBy ?: "Свободна"
                binding.tvReservedBy.visibility = View.VISIBLE
            } else {
                binding.tvReservedBy.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(car)
            }
        }
    }
}
