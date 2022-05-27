package com.example.smartboxpj

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.smartboxpj.databinding.LoginBinding
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.IOException


class LoginActivity : MainActivity() {
    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun showSignUp(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    fun showMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun submitLogin(view: View) {

        val data = FormBody.Builder()
            .add("email", binding.editEmail.text.toString())
            .add("password", binding.editPassword.text.toString())
            .build()

        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))

        val client = OkHttpClient.Builder().cookieJar(cookieManager).build()
        val request: Request =  Request.Builder().url("https://ancient-savannah-30390.herokuapp.com/auth/login/").post(data).build()

        try {
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    println(response)
                    if(response.code != 200){
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Napaka pri prijavi." , Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        runOnUiThread {
                            showMain()
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
}