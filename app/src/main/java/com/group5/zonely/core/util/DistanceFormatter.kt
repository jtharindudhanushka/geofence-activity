package com.group5.zonely.core.util

import com.group5.zonely.domain.model.DistanceUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class DistanceFormatter @Inject constructor() {
    fun format(meters: Float, unit: DistanceUnit): String {
        return when (unit) {
            DistanceUnit.METRIC -> {
                if (meters >= 1000) {
                    val km = meters / 1000f
                    "%.1f km".format(km)
                } else {
                    "${meters.roundToInt()} m"
                }
            }
            DistanceUnit.IMPERIAL -> {
                val feet = meters * 3.28084f
                if (feet >= 5280) {
                    val miles = feet / 5280f
                    "%.1f mi".format(miles)
                } else {
                    "${feet.roundToInt()} ft"
                }
            }
        }
    }
}
