package com.group5.zonely.domain.model

data class SimpleLocation(
    val latitude: Double, 
    val longitude: Double,
    val accuracyMeters: Float, 
    val timeMillis: Long,
)
