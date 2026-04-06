package com.example.servihub.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.servihub.R
import com.example.servihub.model.UserProfile
import java.util.Random

class ProfessionalAdapter(private var professionals: List<UserProfile>) :
    RecyclerView.Adapter<ProfessionalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val service: TextView = view.findViewById(R.id.tvService)
        val rating: TextView = view.findViewById(R.id.tvRating)
        val city: TextView = view.findViewById(R.id.tvCity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_professional_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val professional = professionals[position]
        holder.name.text = professional.fullName
        holder.service.text = professional.serviceType
        holder.city.text = professional.city
        
        val displayRating = if (professional.rating > 0) {
            professional.rating.toString()
        } else {
            String.format("%.1f", 4.0 + Random().nextFloat())
        }
        holder.rating.text = displayRating

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfessionalDetailActivity::class.java).apply {
                putExtra("PRO_NAME", professional.fullName)
                putExtra("PRO_SPECIALTY", professional.serviceType)
                putExtra("PRO_RATING", displayRating)
                putExtra("PRO_LOCATION", professional.city)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = professionals.size

    fun updateList(newList: List<UserProfile>) {
        professionals = newList
        notifyDataSetChanged()
    }
}
