package com.example.smartboxpj

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.example.smartboxpj.databinding.ActivityHistoryBinding
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime

class HistoryActivity : MainActivity() {

    private fun getDataFromAPI(ctx: Context) {
        val cookieCache = SharedPrefsCookiePersistor(ctx).loadAll();

        val cookie = cookieCache.find { it.name == "refreshToken" }
        val userID = JWT(cookie!!.value).getClaim("id").asString()

        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        val client = OkHttpClient.Builder().addInterceptor(myInterceptor(applicationContext)).cookieJar(cookieManager).build()
        val request: Request =  Request.Builder().url("https://ancient-savannah-30390.herokuapp.com/users/$userID/unlocks").build()

        try {
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    println(response.message)
                    if(response.code != 200){
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Napaka pri prijavi." , Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        runOnUiThread {

                        }
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    println(e.localizedMessage)
                }
            })
        } catch (e: Exception){
            println(e.message)
        }
    }

    private lateinit var binding: ActivityHistoryBinding
    val data = mutableListOf<ItemsViewModel>()
    val adapter = HistoryAdapter(data)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataFromAPI(applicationContext)

        for(i in 1..20){
        data.add(ItemsViewModel("My title", LocalDateTime.now(), true, R.drawable.ic_boxicon3))
        }
        val recycler = findViewById<RecyclerView>(R.id.history_recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }
}