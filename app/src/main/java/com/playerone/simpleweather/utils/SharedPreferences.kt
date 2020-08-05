package com.playerone.simpleweather.utils

import android.content.Context
import androidx.preference.PreferenceManager

class SharedPreferences(context: Context) {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    var temperatureUnit: String
        get() = preferences.getString("temperatureUnit", "Celsius").toString()
        set(value) = preferences.edit().putString("temperatureUnit", value).apply()

    var currentLocation: String
        get() = preferences.getString("currentLocation", "").toString()
        set(value) = preferences.edit().putString("currentLocation", value).apply()

    var useGPS: Boolean
        get() = preferences.getBoolean("useGPS", false)
        set(value) = preferences.edit().putBoolean("useGPS", value).apply()

}