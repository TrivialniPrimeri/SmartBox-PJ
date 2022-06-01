package com.example.smartboxpj

import Util.ImageUtil
import android.R.attr.bitmap
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.example.smartboxpj.databinding.LoginBinding
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class LoginActivity : MainActivity() {
    private lateinit var binding: LoginBinding
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            Toast.makeText(applicationContext, "Slika uspešno zajeta." , Toast.LENGTH_SHORT).show()
            val convertedImg = ImageUtil.convert(imageBitmap)

            val data = FormBody.Builder()
                .add("loginImage", convertedImg)
                .build()

            val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
            val client = OkHttpClient.Builder().cookieJar(cookieManager).build()
            val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/auth/login/").post(data).build()

            try {
                client.newCall(request).enqueue(object: Callback {
                    override fun onResponse(call: Call, response: Response) {
                        println(response)
                        if(response.code != 200){
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Napaka pri prijavi s kamero." , Toast.LENGTH_SHORT).show()
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

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_ID_MULTIPLE_PERMISSIONS)

        } catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, "Napaka pri odpiranju kamere." , Toast.LENGTH_SHORT).show()
        }
    }

    fun cameraLogin(view: View){
        try{
            dispatchTakePictureIntent()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, "Napaka pri odpiranju kamere." , Toast.LENGTH_SHORT).show()
        }
    }

    fun submitLogin(view: View) {

        val data = FormBody.Builder()
            .add("email", binding.editEmail.text.toString())
            .add("password", binding.editPassword.text.toString())
            .build()

        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))

        val client = OkHttpClient.Builder().cookieJar(cookieManager).build()
        val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/auth/login/").post(data).build()

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