package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import javax.inject.Inject

class DeleteZoneUseCase @Inject constructor(
    private val repository: GeofenceZoneRepository,
    private val registrar: GeofenceRegistrar
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        registrar.unregister(id)
        repository.delete(id)
        return Result.success(Unit)
    }
}
