package com.playerone.simpleweather.model.forecast


import com.google.gson.annotations.SerializedName

data class ForecastWeatherResponse(
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int
)