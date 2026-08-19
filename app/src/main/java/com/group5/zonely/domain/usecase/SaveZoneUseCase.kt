package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import javax.inject.Inject

class SaveZoneUseCase @Inject constructor(
    private val repository: GeofenceZoneRepository
) {
    suspend operator fun invoke(zone: GeofenceZone): Result<Unit> {
        // Basic validation
        if (zone.name.isBlank()) return Result.failure(Exception("Name cannot be empty"))
        if (zone.radiusMeters < 50) return Result.failure(Exception("Radius too small"))
        
        repository.upsert(zone)
        return Result.success(Unit)
    }
}
