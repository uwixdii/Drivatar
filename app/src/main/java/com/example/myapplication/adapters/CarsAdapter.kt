    package com.example.myapplication.adapters

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.example.myapplication.databinding.ItemCarBinding
    import com.example.myapplication.models.Car
    import com.google.firebase.auth.FirebaseAuth

    class CarsAdapter(
        private val carsList: MutableList<Car>,
        private val isAdmin: Boolean = false,
        private val onDetailsClick: (Car) -> Unit,
        private val onBookClick: (Car) -> Unit,
        private val onCancelReservationClick: (Car) -> Unit,
        private val onHideClick: (Car) -> Unit,
        private val onDeleteClick: (Car) -> Unit
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
                binding.carName.text = car.name
                binding.carYear.text = "Год: ${car.year}"
                binding.carPrice.text = "Цена: ${car.price} $"
                binding.carColor.text = "Цвет: ${car.color}"
                binding.carMileage.text = "Пробег: ${car.mileage} км"

                // Логика для кнопок
                binding.detailsButton.setOnClickListener {
                    onDetailsClick(car)
                }

                // Кнопка "Забронировать" только если машина не забронирована
                if (car.reservedBy == null) {
                    binding.bookButton.visibility = View.VISIBLE
                    binding.bookButton.setOnClickListener {
                        onBookClick(car)
                    }
                } else {
                    binding.bookButton.visibility = View.GONE
                }

                // Убираем кнопку "Отменить бронирование" из адаптера
                binding.cancelReservationButton.visibility = View.GONE
            }
        }
    }
