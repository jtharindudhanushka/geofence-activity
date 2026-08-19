package com.group5.zonely.domain.repository

import com.group5.zonely.domain.model.GeofenceEvent
import kotlinx.coroutines.flow.Flow

interface GeofenceEventRepository {
    fun observeEvents(limit: Int = 200): Flow<List<GeofenceEvent>>
    fun observeEventsForZone(zoneId: String): Flow<List<GeofenceEvent>>
    suspend fun record(event: GeofenceEvent)
    suspend fun clearAll()
}
