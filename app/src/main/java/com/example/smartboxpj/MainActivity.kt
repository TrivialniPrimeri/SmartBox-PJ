package com.example.smartboxpj


import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.auth0.android.jwt.JWT
import com.example.smartboxpj.databinding.ActivityMainBinding
import com.example.smartboxpj.databinding.LoginBinding
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import kotlin.math.log

data class API_Response(val data: String, val result: String, val errorNumber: String){}

open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val cookieCache = SharedPrefsCookiePersistor(this).loadAll()
        var loggedIn = false
        for (cookie in cookieCache) {
            if(cookie.name == "refreshToken" && cookie.persistent){
                loggedIn = true
            }
        }

        if(!loggedIn){
            menu!!.getItem(0).setEnabled(false)
            menu!!.getItem(0).getIcon().setAlpha(100)
            menu!!.getItem(1).setEnabled(false)
            menu!!.getItem(1).getIcon().setAlpha(100)
        }
        else{
            menu!!.getItem(0).setEnabled(true)
            menu!!.getItem(0).getIcon().setAlpha(255)
            menu!!.getItem(1).setEnabled(true)
            menu!!.getItem(1).getIcon().setAlpha(255)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.menu_profile){
            showProfile()
            return true
        }
        if(id == R.id.menu_history){
            showHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cookieCache = SharedPrefsCookiePersistor(this).loadAll()
        var loggedIn = false
        for (cookie in cookieCache) {
            if(cookie.name == "refreshToken" && cookie.persistent){
                loggedIn = true
            }
        }

        binding.button.isEnabled = loggedIn
        binding.button4.isEnabled = !loggedIn
    }

    fun unlockFeedback(state: Boolean, boxId: String?){


        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        val client = OkHttpClient.Builder().addInterceptor(myInterceptor(applicationContext)).cookieJar(cookieManager).build()

        val cookieCache = SharedPrefsCookiePersistor(applicationContext).loadAll();
        val cookie = cookieCache.find { it.name == "refreshToken" }
        val userID = JWT(cookie!!.value).getClaim("id").asString()

        if(boxId == null || userID == null) return

        val postData = FormBody.Builder().add("success", state.toString()).add("boxId", boxId).add("userId", userID).build()
        val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/unlocks").post(postData).build()

        try {
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                }
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Napaka pri odklepu." , Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception){
            Toast.makeText(applicationContext, "Napaka pri odklepu." , Toast.LENGTH_SHORT).show()
        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            var id = result.data?.getStringExtra("id")

            try{
                id = id!!.split("/").toTypedArray()[1]
                if(id.substring(0,3) == "000"){
                    id = id.drop(3)
                }
            } catch (e: Exception){
                Toast.makeText(this, "QR koda ni veljavnega formata za paketnik.", Toast.LENGTH_SHORT)
            }

            val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
            val client = OkHttpClient.Builder().addInterceptor(myInterceptor(applicationContext)).cookieJar(cookieManager).build()
            val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/box/unlock/$id").build()
            try {
                client.newCall(request).enqueue(object: Callback {
                    override fun onResponse(call: Call, response: Response) {

                        if(response.code == 403){
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Nimate pravice za odklep!." ,Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val body = response?.body?.string()
                        val gson = GsonBuilder().create()
                        val resp = gson.fromJson(body, API_Response::class.java)

                        if(resp.errorNumber == "0"){
                            val fos = FileOutputStream(File(filesDir, "token.mp3"))
                            fos.write(Base64.decode(resp.data.toByteArray(), Base64.DEFAULT))
                            fos.close()
                            val player = MediaPlayer.create(applicationContext, Uri.parse("$filesDir/token.mp3"))
                            player.start()

                            player.setOnCompletionListener {
                                val builder = AlertDialog.Builder(this@MainActivity)
                                    .setCancelable(false)
                                    .setTitle("Ali ste uspešno odklenili paketnik?")
                                    .setPositiveButton("Da") { dialog, which ->
                                        unlockFeedback(true, id)
                                    }
                                    .setNegativeButton("Ne") { dialog, which ->
                                        unlockFeedback(false, id)
                                    }
                                    .show()
                            }
                        }
                        else{
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Napaka pri pridobivanju žetona." ,Toast.LENGTH_SHORT).show()
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

    open fun showSignUp(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    open fun showSignIn(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    open fun showSignIn() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun showHistory(){
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    fun showProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }


}