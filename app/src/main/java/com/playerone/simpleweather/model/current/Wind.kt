package com.playerone.simpleweather.model.current


import com.google.gson.annotations.SerializedName

data class Wind(
    val deg: Int,
    val speed: Double
)