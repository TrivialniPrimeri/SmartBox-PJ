package com.example.smartboxpj


import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

data class API_Response(val data: String, val result: String, val errorNumber: String){}

open class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val id = result.data?.getStringExtra("id")



            //Toast.makeText(this, "Id: $id" ,Toast.LENGTH_SHORT).show()
            // Handle the Intent

            val client = OkHttpClient()
            val request: Request =  Request.Builder().url("https://ancient-savannah-30390.herokuapp.com/box/unlock/$id").build()
            try {
                client.newCall(request).enqueue(object: Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val body = response?.body?.string()
                        val gson = GsonBuilder().create()
                        val resp = gson.fromJson(body, API_Response::class.java)

                        if(resp.errorNumber != null){ //todo: individual error messages for error codes (403, 404...)
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Napaka pri pridobivanju žetona." ,Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            val fos = FileOutputStream(File(filesDir, "test.mp3"))
                            fos.write(Base64.decode(resp.data.toByteArray(), Base64.DEFAULT))
                            fos.close()
                            val player = MediaPlayer.create(applicationContext, Uri.parse("$filesDir/test.mp3"))
                            player.start()

                            player.setOnCompletionListener {
                                Toast.makeText(applicationContext, "Koncal sem!" ,Toast.LENGTH_SHORT).show()
                                //send request to API to save stuff
                            }

                        }

                    }

                    override fun onFailure(call: Call, e: IOException) {
                        println(e.localizedMessage)
                    }
                })
            } catch (e: Exception){
                println(e.message)
                Toast.makeText(this, "Neuspešno skeniranje" ,Toast.LENGTH_SHORT).show()
            }


        }
    }

    fun scanQrCode(view: View){
        val intent = Intent(this, QrScannerActivity::class.java)
        getContent.launch(intent)
    }

    fun showSignUp(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

}