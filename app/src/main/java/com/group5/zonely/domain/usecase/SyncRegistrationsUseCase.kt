package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import javax.inject.Inject

class SyncRegistrationsUseCase @Inject constructor(
    private val repository: GeofenceZoneRepository,
    private val registrar: GeofenceRegistrar
) {
    suspend operator fun invoke(): Result<Unit> {
        val activeZones = repository.getActiveZones()
        return registrar.reregisterAll(activeZones)
    }
}
