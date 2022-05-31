package com.example.smartboxpj

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.auth0.android.jwt.JWT
import com.example.smartboxpj.databinding.LoginBinding
import com.example.smartboxpj.databinding.ProfileBinding
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset

data class profileResponse(
    val _id: String,
    val name: String,
    val surname: String,
    val email: String,
    val phone: String,
    val address: String,
    val boxCount: String,
    val authorizedCount: String,
)

class ProfileActivity : MainActivity() {
    private lateinit var binding: ProfileBinding

    private fun getDataFromAPI(ctx: Context) {

        val cookieCache = SharedPrefsCookiePersistor(ctx).loadAll();

        val cookie = cookieCache.find { it.name == "refreshToken" }
        val userID = JWT(cookie!!.value).getClaim("id").asString()

        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        val client = OkHttpClient.Builder().addInterceptor(myInterceptor(applicationContext)).cookieJar(cookieManager).build()
        val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/users/$userID/").build()

        try {
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    if(response.code != 200){
                        println(response.code)
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Napaka pri pridobivanju profila 0." , Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        runOnUiThread {
                            val obj = Gson().fromJson(response.body!!.string(), profileResponse::class.java)
                            binding.progressBar.visibility = View.GONE
                            binding.newEmail.setText(obj.email)
                            binding.newPhoneNumber.setText(obj.phone)
                            binding.newAddress.setText(obj.address)
                            binding.textView2.text = "${obj.name} ${obj.surname}"
                            binding.textView13.setText(obj.boxCount)
                            binding.textView18.setText(obj.authorizedCount)
                        }
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Napaka pri pridobivanju profila 1." , Toast.LENGTH_SHORT).show()
                    }

                }
            })
        } catch (e: Exception){
            Toast.makeText(applicationContext, "Napaka pri pridobivanju profila 2." , Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.VISIBLE
        getDataFromAPI(applicationContext)


    }



}