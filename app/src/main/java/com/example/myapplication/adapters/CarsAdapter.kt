package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCarBinding
import com.example.myapplication.models.Car

class CarsAdapter(
    private val carsList: List<Car>,
    private val onDetailsClick: (Car) -> Unit,
    private val onBookClick: (Car) -> Unit,
    private val onCancelReservationClick: (Car) -> Unit // Новый обработчик для отмены бронирования
) : RecyclerView.Adapter<CarsAdapter.CarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carsList[position]
        holder.bind(car)
    }

    override fun getItemCount(): Int = carsList.size

    inner class CarViewHolder(private val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) {
            binding.tvCarName.text = car.name
            binding.tvCarYear.text = "Год: ${car.year}"
            binding.tvCarPrice.text = "Цена: ${car.price} $"
            binding.tvCarColor.text = "Цвет: ${car.color}"
            binding.tvCarMileage.text = "Пробег: ${car.mileage} км"

            if (car.reservedBy.isNullOrEmpty()) {
                // Машина не забронирована
                binding.btnBook.visibility = View.VISIBLE
                binding.btnCancelReservation.visibility = View.GONE
                binding.btnBook.text = "Забронировать"
                binding.btnBook.setOnClickListener {
                    onBookClick(car)
                }
            } else {
                // Машина забронирована
                binding.btnBook.visibility = View.GONE
                binding.btnCancelReservation.visibility = View.VISIBLE
                binding.btnCancelReservation.text = "Отменить бронирование"
                binding.btnCancelReservation.setOnClickListener {
                    onCancelReservationClick(car)
                }
            }

            // Кнопка "Подробнее"
            binding.btnDetails.setOnClickListener {
                onDetailsClick(car)
            }
        }
    }
}
