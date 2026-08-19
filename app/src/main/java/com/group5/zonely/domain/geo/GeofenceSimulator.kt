package com.group5.zonely.domain.geo

import com.group5.zonely.domain.model.TransitionType

interface GeofenceSimulator {
    suspend fun simulateTransition(zoneId: String, transition: TransitionType)
}
