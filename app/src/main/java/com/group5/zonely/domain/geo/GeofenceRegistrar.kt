package com.group5.zonely.domain.geo

import com.group5.zonely.domain.model.GeofenceZone

interface GeofenceRegistrar {
    suspend fun register(zone: GeofenceZone): Result<Unit>
    suspend fun unregister(zoneId: String): Result<Unit>
    suspend fun reregisterAll(zones: List<GeofenceZone>): Result<Unit>
    suspend fun unregisterAll(): Result<Unit>
}
