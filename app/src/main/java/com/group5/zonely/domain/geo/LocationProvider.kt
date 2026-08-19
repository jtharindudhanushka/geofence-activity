package com.group5.zonely.domain.geo

import com.group5.zonely.domain.model.SimpleLocation
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    suspend fun currentLocation(): Result<SimpleLocation>
    fun locationUpdates(intervalMillis: Long = 5_000L): Flow<SimpleLocation>
}
