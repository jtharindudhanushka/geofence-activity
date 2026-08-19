package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.model.GeofenceEvent
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.domain.repository.GeofenceEventRepository
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import javax.inject.Inject

class RecordTransitionUseCase @Inject constructor(
    private val zoneRepository: GeofenceZoneRepository,
    private val eventRepository: GeofenceEventRepository
) {
    suspend operator fun invoke(
        zoneId: String,
        transition: TransitionType,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Float? = null,
        timeMillis: Long = System.currentTimeMillis()
    ): Result<Unit> {
        val zone = zoneRepository.getZone(zoneId) ?: return Result.failure(Exception("Zone not found"))
        
        val event = GeofenceEvent(
            zoneId = zoneId,
            zoneName = zone.name,
            transition = transition,
            occurredAt = timeMillis,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracy
        )
        
        eventRepository.record(event)
        return Result.success(Unit)
    }
}
