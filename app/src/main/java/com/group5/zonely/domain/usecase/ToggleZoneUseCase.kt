package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import javax.inject.Inject

class ToggleZoneUseCase @Inject constructor(
    private val repository: GeofenceZoneRepository,
    private val registrar: GeofenceRegistrar
) {
    suspend operator fun invoke(id: String, active: Boolean): Result<Unit> {
        repository.setActive(id, active)
        val zone = repository.getZone(id) ?: return Result.failure(Exception("Zone not found"))
        
        return if (active) {
            registrar.register(zone)
        } else {
            registrar.unregister(id)
        }
    }
}
