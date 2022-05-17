package com.example.smartboxpj

import android.os.Bundle
import com.example.smartboxpj.databinding.RegisterBinding

class RegisterActivity : MainActivity(){
    private lateinit var binding: RegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}