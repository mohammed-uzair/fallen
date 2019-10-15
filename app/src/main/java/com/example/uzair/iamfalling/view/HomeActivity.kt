package com.example.uzair.iamfalling.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uzair.fallen.events_service.FallDetectionService
import com.example.uzair.iamfalling.AppHandler
import com.example.uzair.iamfalling.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        init()
    }

    override fun onDestroy() {
        stopService()

        super.onDestroy()
    }

    private fun init() {
        //Move to home menu fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_home_activity, HomeMenuFragment(), "HomeMenuFragment")
            .addToBackStack("HomeMenuFragment")
            .commit()

        startService()
    }

    private fun startService() {
        val application = application as AppHandler

        //Give the fallen library all the notification messages
        application.fallen.setFallDetectionMessages(resources.getStringArray(R.array.fall_detected_messages))
        application.fallen.setFrequentFallDetectionMessages(resources.getStringArray(R.array.frequent_fall_messages))
        application.fallen.setShakeDetectionMessages(resources.getStringArray(R.array.shake_messages))

        application.fallen.startEventDetectionService()
    }

    private fun stopService() {
        val intent = Intent(this, FallDetectionService::class.java)
        stopService(intent)
    }
}