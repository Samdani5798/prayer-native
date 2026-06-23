package com.realislamic.prayertimes.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.toDegrees
import kotlin.math.toRadians

object QiblaUtil {

    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        val lat1 = toRadians(userLat)
        val lat2 = toRadians(KAABA_LAT)
        val deltaLng = toRadians(KAABA_LNG - userLng)

        val y = sin(deltaLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)

        var bearing = toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        return bearing
    }

    fun bearingToCardinal(bearing: Double): String {
        val directions = arrayOf(
            "North", "North-East", "East", "South-East",
            "South", "South-West", "West", "North-West"
        )
        val index = (((bearing + 22.5) / 45.0).toInt()) % 8
        return directions[index]
    }
}
