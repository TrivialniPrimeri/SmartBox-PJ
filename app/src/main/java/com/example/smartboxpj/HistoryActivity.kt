package com.example.smartboxpj

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.example.smartboxpj.databinding.ActivityHistoryBinding
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class Box (
    val nickname: String,
    val boxId: String,
)

data class historyResponse(
    val _id: String,
    val success: Boolean,
    val boxId: Box,
    val createdAt: String
)

class HistoryActivity : MainActivity() {

    private fun getDataFromAPI(ctx: Context, data: MutableList<ItemsViewModel>, adapter: HistoryAdapter) {

        val cookieCache = SharedPrefsCookiePersistor(ctx).loadAll();

        val cookie = cookieCache.find { it.name == "refreshToken" }
        val userID = JWT(cookie!!.value).getClaim("id").asString()

        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        val client = OkHttpClient.Builder().addInterceptor(myInterceptor(applicationContext)).cookieJar(cookieManager).build()
        val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/users/$userID/unlocks").build()

        try {
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    if(response.code != 200){
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Napaka pri pridobivanju zgodovine." , Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        runOnUiThread {
                            if(response.body!!.string() == "[]"){
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(applicationContext, "Nimate odklepov.", Toast.LENGTH_LONG).show()
                            }
                            else{
                                val obj = Gson().fromJson(response.body!!.string(), Array<historyResponse>::class.java)
                                obj.forEach {
                                    data.add(
                                        ItemsViewModel(
                                            "${it.boxId.nickname} (${it.boxId.boxId})",
                                            Instant.parse(it.createdAt).atZone(ZoneOffset.UTC).toLocalDateTime(),
                                            it.success,
                                            if (it.success) R.drawable.ic_openboxsuccess else R.drawable.ic_openboxfail
                                        )
                                    )
                                    adapter.notifyItemInserted(data.size - 1)
                                    binding.progressBar.visibility = View.GONE
                                }
                            }

                        }
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    binding.progressBar.visibility = View.GONE
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Napaka pri pridobivanju zgodovine." , Toast.LENGTH_SHORT).show()
                    }

                }
            })
        } catch (e: Exception){
            binding.progressBar.visibility = View.GONE
            Toast.makeText(applicationContext, "Napaka pri pridobivanju zgodovine." , Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var binding: ActivityHistoryBinding
    var data = mutableListOf<ItemsViewModel>()
    val adapter = HistoryAdapter(data)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.VISIBLE
        getDataFromAPI(applicationContext, data, adapter)

        val recycler = findViewById<RecyclerView>(R.id.history_recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }
}