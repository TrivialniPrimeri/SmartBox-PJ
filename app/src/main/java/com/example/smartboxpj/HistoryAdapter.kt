package com.example.smartboxpj

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class ItemsViewModel(val title: String, val date: LocalDateTime,var success: Boolean, val image: Int) {
}
class HistoryAdapter(private val myList: List<ItemsViewModel>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        val itemsViewModel = myList[position]
        holder.title.text = itemsViewModel.title
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        holder.date.text = itemsViewModel.date.format(dateTimeFormatter)
        holder.success.text = itemsViewModel.success.toString()
        holder.imageView.setImageResource(itemsViewModel.image)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val title: TextView = itemView.findViewById(R.id.cardTitle)
        val date: TextView = itemView.findViewById(R.id.unlockDateId)
        val success: TextView = itemView.findViewById(R.id.successId)
        val imageView: ImageView = itemView.findViewById(R.id.cardImage)
    }
}