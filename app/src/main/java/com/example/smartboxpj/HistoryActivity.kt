package com.example.smartboxpj

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartboxpj.databinding.ActivityHistoryBinding
import java.time.LocalDateTime

class HistoryActivity : MainActivity() {
    private lateinit var binding: ActivityHistoryBinding
    val data = mutableListOf<ItemsViewModel>()
    val adapter = HistoryAdapter(data)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        for(i in 1..20){
        data.add(ItemsViewModel("My title", LocalDateTime.now(), true, R.drawable.ic_boxicon3))
        }
        val recycler = findViewById<RecyclerView>(R.id.history_recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }
}