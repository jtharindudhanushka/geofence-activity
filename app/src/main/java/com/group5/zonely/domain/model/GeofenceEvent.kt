package com.group5.zonely.domain.model

data class GeofenceEvent(
    val id: Long = 0L,
    val zoneId: String,
    val zoneName: String,
    val transition: TransitionType,
    val occurredAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
)
