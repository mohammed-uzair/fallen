package com.example.uzair.iamfalling.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.uzair.iamfalling.R

const val SPLASH_SCREEN_DELAY_MILLISECONDS = 1000

/**
 * This is the launch activity, performs a simple circular reveal
 */
class LaunchActivity : Activity() {
    private var constraintLayout: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        init()
    }

    override fun onResume() {
        super.onResume()

        constraintLayout!!.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                v.removeOnLayoutChangeListener(this)

                //Prepare for circular revel
                val revealX = constraintLayout!!.width / 2
                val revealY = constraintLayout!!.height / 2

                val finalRadius = Math.hypot(revealX.toDouble(), revealY.toDouble()).toFloat()

                val anim = ViewAnimationUtils.createCircularReveal(
                    constraintLayout,
                    revealX,
                    revealY,
                    0f,
                    finalRadius
                )

                //Make the root view visible
                constraintLayout!!.visibility = View.VISIBLE

                //Start the animation for circular revel
                anim.start()
            }
        })
    }

    private fun init() {
        //Initialize the view
        constraintLayout = findViewById(R.id.anim_view)

        //Open the main home screen after some pre defined seconds
        Handler().postDelayed({
            startActivity(Intent(this@LaunchActivity, HomeActivity::class.java))

            this@LaunchActivity.finish()
        }, SPLASH_SCREEN_DELAY_MILLISECONDS.toLong())
    }
}

