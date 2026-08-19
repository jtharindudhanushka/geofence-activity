package com.group5.zonely.geo

import com.group5.zonely.domain.geo.GeofenceSimulator
import com.group5.zonely.domain.model.TransitionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceSimulatorNoOp @Inject constructor() : GeofenceSimulator {
    override suspend fun simulateTransition(zoneId: String, transition: TransitionType) {
        // No-op in release
    }
}
