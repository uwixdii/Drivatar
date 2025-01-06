import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Car

class CarsAdapter(private val cars: List<Car>) : RecyclerView.Adapter<CarsAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val year: TextView = itemView.findViewById(R.id.tvYear)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.name.text = car.name
        holder.year.text = car.year
        holder.price.text = car.price
    }

    override fun getItemCount(): Int = cars.size
}
