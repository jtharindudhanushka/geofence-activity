package com.group5.zonely.domain.model

data class GeofenceZone(
    val id: String,                  // UUID string; also the geofence requestId
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val transitionTypes: Set<TransitionType> = setOf(TransitionType.ENTER, TransitionType.EXIT),
    val loiteringDelayMillis: Int = 60_000,
    val isActive: Boolean = true,
    val colorArgb: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
