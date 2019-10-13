package com.example.uzair.iamfalling

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    override fun onDestroy() {
        stopService()

        super.onDestroy()
    }

    private fun init() {
        startService()
    }

    private fun startService() {
        val intent = Intent(this, FallDetectionService::class.java)
        intent.putExtra("inputExtra", "I am your life saver, please let me run silently")

        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopService() {
        val intent = Intent(this, FallDetectionService::class.java)
        stopService(intent)
    }
}