package com.example.smartboxpj

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.smartboxpj.databinding.LoginBinding

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
}