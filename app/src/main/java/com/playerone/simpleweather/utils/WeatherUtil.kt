package com.playerone.simpleweather.utils

import com.playerone.simpleweather.R

class WeatherUtil {

    fun getIcon(icon: String): Int {
        return when (icon) {
            "01d" -> R.drawable.clear_sky

            "01n" -> R.drawable.clear_sky_night

            "02d" -> R.drawable.few_clouds

            "02n" -> R.drawable.few_clouds_night

            "03d", "03n" -> R.drawable.scattered_clouds

            "04d", "04n" -> R.drawable.broken_clouds

            "09d", "09n" -> R.drawable.shower_rain

            "10d" -> R.drawable.rain

            "10n" -> R.drawable.rain_night

            "11d", "11n" -> R.drawable.thunderstorm

            "13d", "13n" -> R.drawable.snow

            "50d", "50n" -> R.drawable.mist

            else -> 0
        }
    }

    fun imgPath(icon: String): List<String> {
        val photoId: String
        if (icon == "01d") {
            //cielo despejado
            photoId = "XsI9XCjbnvQ"
            return listOf(photoId, photoBy("Joel LIU", photoId))
        } else if (icon == "01n") {
            //cielo despejado de noche
            photoId = "_bVXFL8q2XI"
            return listOf(photoId, photoBy("Touann Gatouillat", photoId))
        } else if (icon == "02d") {
            //pocas nubes
            photoId = "0ubN_9HHILs"
            return listOf(photoId, photoBy("Wang Binghua", photoId))
        } else if (icon == "02n") {
            //pocas nubes de noche Rch4nisgc9Y
            photoId = "ciahwyO2Y38"
            return listOf(photoId, photoBy("Tom Coe", photoId))
        } else if (icon == "03d") {
            //nubes dispersas
            photoId = "VS5UacvWrc0"
            return listOf(photoId, photoBy("Alfred Kenneally", photoId))
        } else if (icon == "03n") {
            //nubes dispersas de noche
            photoId = "ymJt1_jLreY"
            return listOf(photoId, photoBy("Gavin Spear", photoId))
        } else if (icon == "04d") {
            //nubes rotas
            photoId = "gyHEQ6_dvPw"
            return listOf(photoId, photoBy("Eric Cook", photoId))
        } else if (icon == "04n") {
            //nubes rotas noche
            photoId = "hgGplX3PFBg"
            return listOf(photoId, photoBy("Tom Barrett", photoId))
        } else if (icon == "09d" || icon == "10d") {
            //lluvia dia
            photoId = "Fs1ehbtXZjc"
            return listOf(photoId, photoBy("Thomas Charters", photoId))
        } else if (icon == "09n" || icon == "10n") {
            //lluvia noche
            photoId = "_zUAcIvs-ME"
            return listOf(photoId, photoBy("Norbert Tóth", photoId))
        } else if (icon == "11d" || icon == "11n") {
            //lluvia noche
            photoId = "uu-Jw5SunYI"
            return listOf(photoId, photoBy("Max LaRochelle", photoId))
        } else if (icon == "13d" || icon == "13n") {
            photoId = "GtcbqalW0bQ"
            return listOf(photoId, photoBy("Genesis Marasigan", photoId))
        } else if (icon == "50d" || icon == "50n") {
            photoId = "7CME6Wlgrdk"
            return listOf(photoId, photoBy("Annie Spratt", photoId))
        } else return listOf()

    }

    fun photoBy(autorName: String, photoId: String): String {
        return "<span>Photo by <a href=\"https://unsplash.com/photos/" +
                "${photoId}?\">${autorName}</a> on " +
                "<a href=\"https://unsplash.com/\">Unsplash</a></span>"

    }
}