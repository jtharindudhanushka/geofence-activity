package com.group5.zonely.data.repository

import com.group5.zonely.domain.model.GeofenceEvent
import com.group5.zonely.domain.repository.GeofenceEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryGeofenceEventRepository @Inject constructor() : GeofenceEventRepository {

    private val events = MutableStateFlow<List<GeofenceEvent>>(emptyList())
    private val limit = 200

    override fun observeEvents(limit: Int): Flow<List<GeofenceEvent>> = 
        events.map { it.take(limit) }

    override fun observeEventsForZone(zoneId: String): Flow<List<GeofenceEvent>> =
        events.map { list -> list.filter { it.zoneId == zoneId } }

    override suspend fun record(event: GeofenceEvent) {
        events.update { current ->
            val nextId = (current.firstOrNull()?.id ?: 0L) + 1
            val newEvent = event.copy(id = nextId)
            (listOf(newEvent) + current).take(limit)
        }
    }

    override suspend fun clearAll() {
        events.value = emptyList()
    }
}
