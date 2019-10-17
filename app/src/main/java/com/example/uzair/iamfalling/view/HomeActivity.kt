package com.example.uzair.iamfalling.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uzair.fallen.Fallen
import com.example.uzair.iamfalling.AppHandler
import com.example.uzair.iamfalling.R

/**
 * The base and the single activity for this app, apart the launch activity
 */
class HomeActivity : AppCompatActivity() {
    private lateinit var fallen: Fallen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        init()
    }

    /**
     * Initialization method
     */
    private fun init() {
        //Move to home menu fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_home_activity, HomeMenuFragment(), "HomeMenuFragment")
            .commit()

        fallen = (application as AppHandler).fallen
    }

    /**
     * Start the fallen library to listen for the device events
     */
    fun startFallen(
        detectFalls: Boolean = false,
        detectShakes: Boolean = false,
        detectFrequentFalls: Boolean = false
    ) {
        //Give the fallen library all the notification messages
        fallen.setFallDetectionMessages(resources.getStringArray(R.array.fall_detected_messages))
        fallen.setFrequentFallDetectionMessages(resources.getStringArray(R.array.frequent_fall_messages))
        fallen.setShakeDetectionMessages(resources.getStringArray(R.array.shake_messages))

        fallen.startFallen(
            detectFalls = detectFalls,
            detectShakes = detectShakes,
            detectFrequentFalls = detectFrequentFalls
        )
    }

    /**
     * Stop the fallen and close its services
     */
    fun stopFallen() {
        fallen.stopFallen()
    }
}