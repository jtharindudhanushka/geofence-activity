package com.group5.zonely.geo

import com.group5.zonely.domain.geo.GeofenceSimulator
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.domain.usecase.RecordTransitionUseCase
import com.group5.zonely.notification.TransitionNotifier
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceSimulatorImpl @Inject constructor(
    private val recordTransitionUseCase: RecordTransitionUseCase,
    private val transitionNotifier: TransitionNotifier
) : GeofenceSimulator {
    override suspend fun simulateTransition(zoneId: String, transition: TransitionType) {
        recordTransitionUseCase(
            zoneId = zoneId,
            transition = transition
        )
        transitionNotifier.showTransitionNotification(zoneId, transition)
    }
}
