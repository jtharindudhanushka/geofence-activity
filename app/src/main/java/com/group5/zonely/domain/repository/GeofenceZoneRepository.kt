package com.group5.zonely.domain.repository

import com.group5.zonely.domain.model.GeofenceZone
import kotlinx.coroutines.flow.Flow

interface GeofenceZoneRepository {
    fun observeZones(): Flow<List<GeofenceZone>>
    fun observeZone(id: String): Flow<GeofenceZone?>
    suspend fun getZone(id: String): GeofenceZone?
    suspend fun getActiveZones(): List<GeofenceZone>
    suspend fun upsert(zone: GeofenceZone)          // S1
    suspend fun delete(id: String)                  // S1
    suspend fun setActive(id: String, active: Boolean)
}
