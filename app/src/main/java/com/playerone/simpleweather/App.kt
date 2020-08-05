package com.playerone.simpleweather

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.playerone.simpleweather.network.VolleyGet

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode())
        VolleyGet.init(this)
    }
}