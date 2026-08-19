package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveZonesUseCase @Inject constructor(
    private val repository: GeofenceZoneRepository
) {
    operator fun invoke(): Flow<List<GeofenceZone>> = repository.observeZones()
}
