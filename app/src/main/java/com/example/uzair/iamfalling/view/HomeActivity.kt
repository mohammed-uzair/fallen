package com.example.uzair.iamfalling.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uzair.fallen.Fallen
import com.example.uzair.iamfalling.AppHandler
import com.example.uzair.iamfalling.R

class HomeActivity : AppCompatActivity() {
    private lateinit var fallen: Fallen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        init()
    }

    override fun onDestroy() {
        stopFallen()

        super.onDestroy()
    }

    private fun init() {
        //Move to home menu fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_home_activity, HomeMenuFragment(), "HomeMenuFragment")
            .addToBackStack("HomeMenuFragment")
            .commit()

        fallen = (application as AppHandler).fallen

        startFallen()
    }

    public fun startFallen(
        detectFalls: Boolean = true,
        detectShakes: Boolean = true,
        detectFrequentFalls: Boolean = true
    ) {
        //Give the fallen library all the notification messages
        fallen.setFallDetectionMessages(resources.getStringArray(R.array.fall_detected_messages))
        fallen.setFrequentFallDetectionMessages(resources.getStringArray(R.array.frequent_fall_messages))
        fallen.setShakeDetectionMessages(resources.getStringArray(R.array.shake_messages))

        fallen.startFallen(detectFalls = detectFalls, detectShakes = detectShakes)
    }

    public fun stopFallen() {
        fallen.stopFallen()
    }
}