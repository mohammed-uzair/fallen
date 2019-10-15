package com.example.uzair.iamfalling

import android.app.Application
import com.example.uzair.fallen.Fallen

class AppHandler : Application() {
    public lateinit var fallen: Fallen

    override fun onCreate() {
        super.onCreate()

        fallen = Fallen(this)
    }
}