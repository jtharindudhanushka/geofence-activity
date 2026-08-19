package com.group5.zonely.geo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.domain.usecase.RecordTransitionUseCase
import com.group5.zonely.notification.TransitionNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recordTransitionUseCase: RecordTransitionUseCase

    @Inject
    lateinit var transitionNotifier: TransitionNotifier

    @Inject
    lateinit var errorMapper: GeofenceErrorMapper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = errorMapper.getErrorMessage(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        val pendingResult = goAsync()
        scope.launch {
            try {
                val transitionDetails = parseGeofencingEvent(geofencingEvent)
                transitionDetails.forEach { detail ->
                    recordTransitionUseCase(
                        zoneId = detail.zoneId,
                        transition = detail.transition,
                        latitude = detail.latitude,
                        longitude = detail.longitude,
                        accuracy = detail.accuracy,
                        timeMillis = detail.timeMillis
                    )
                    transitionNotifier.showTransitionNotification(detail.zoneId, detail.transition)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing geofence transition", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Extracted parsing logic for unit testing.
     */
    fun parseGeofencingEvent(event: GeofencingEvent): List<TransitionDetail> {
        val transitionType = when (event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> TransitionType.ENTER
            Geofence.GEOFENCE_TRANSITION_EXIT -> TransitionType.EXIT
            Geofence.GEOFENCE_TRANSITION_DWELL -> TransitionType.DWELL
            else -> return emptyList()
        }

        val triggeringGeofences = event.triggeringGeofences ?: return emptyList()
        val location = event.triggeringLocation
        val timeMillis = System.currentTimeMillis()

        return triggeringGeofences.map { geofence ->
            TransitionDetail(
                zoneId = geofence.requestId,
                transition = transitionType,
                latitude = location?.latitude,
                longitude = location?.longitude,
                accuracy = location?.accuracy,
                timeMillis = timeMillis
            )
        }
    }

    data class TransitionDetail(
        val zoneId: String,
        val transition: TransitionType,
        val latitude: Double?,
        val longitude: Double?,
        val accuracy: Float?,
        val timeMillis: Long
    )

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
