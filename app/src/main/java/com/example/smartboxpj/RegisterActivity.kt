package com.example.smartboxpj

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.smartboxpj.databinding.RegisterBinding
import okhttp3.*
import java.io.IOException

class RegisterActivity : MainActivity(){
    private lateinit var binding: RegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    fun submitRegister(view: View) {

        val requiredFields = arrayOf(binding.newName, binding.newSurname, binding.newEmail, binding.newPassword, binding.newRepeatPassword)
        var valid = true
        requiredFields.forEach {
            if(TextUtils.isEmpty(it.text)){
                it.error = "This field is required!"
                valid = false
            }
        }

        if(valid){

            val data = FormBody.Builder()
                .add("name", binding.newName.text.toString())
                .add("surname", binding.newName.text.toString())
                .add("email", binding.newEmail.text.toString())
                .add("password", binding.newPassword.text.toString())
                .add("address", binding.newAddress.text.toString())
                .add("phone", binding.newPhoneNumber.text.toString())
                .build()

            val client = OkHttpClient()
            val request: Request =  Request.Builder().url("https://ancient-savannah-30390.herokuapp.com/auth/register/").post(data).build()
            try {
                client.newCall(request).enqueue(object: Callback {
                    override fun onResponse(call: Call, response: Response) {
                        println(response)
                        if(response.code != 201){
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Napaka pri registraciji." ,Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Uspelo!." ,Toast.LENGTH_SHORT).show()

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
}