package com.example.smartboxpj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.smartboxpj.databinding.QrScannerActivityBinding

class QrScannerActivity : MainActivity(){
    private lateinit var binding: QrScannerActivityBinding
    private lateinit var cameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QrScannerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraHelper = CameraHelper(
            owner = this,
            context = this.applicationContext,
            viewFinder = findViewById(R.id.qrPreview),
            onResult = ::onResult
        )

        cameraHelper.start()

    }

    private fun onResult(result: String) {
        val data = Intent()
        data.putExtra("id", result)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        cameraHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}