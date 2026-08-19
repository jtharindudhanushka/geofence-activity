package com.group5.zonely.data.repository

import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryGeofenceZoneRepository @Inject constructor() : GeofenceZoneRepository {

    private val zones = MutableStateFlow<Map<String, GeofenceZone>>(
        mapOf(
            "home-zone" to GeofenceZone(
                id = "home-zone",
                name = "Home Zone",
                latitude = 6.9271, // Colombo
                longitude = 79.8612,
                radiusMeters = 200f,
                colorArgb = 0xFF4CAF50.toInt(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            "campus-zone" to GeofenceZone(
                id = "campus-zone",
                name = "Campus",
                latitude = 6.9148, // Near UOK area placeholder
                longitude = 79.9729,
                radiusMeters = 300f,
                colorArgb = 0xFF2196F3.toInt(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    )

    override fun observeZones(): Flow<List<GeofenceZone>> = zones.map { it.values.toList() }

    override fun observeZone(id: String): Flow<GeofenceZone?> = zones.map { it[id] }

    override fun getZone(id: String): GeofenceZone? = zones.value[id]

    override suspend fun getActiveZones(): List<GeofenceZone> = zones.value.values.filter { it.isActive }

    override suspend fun upsert(zone: GeofenceZone) {
        zones.update { it + (zone.id to zone) }
    }

    override suspend fun delete(id: String) {
        zones.update { it - id }
    }

    override suspend fun setActive(id: String, active: Boolean) {
        zones.update { current ->
            val zone = current[id] ?: return@update current
            current + (id to zone.copy(isActive = active, updatedAt = System.currentTimeMillis()))
        }
    }
}
