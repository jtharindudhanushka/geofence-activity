package com.group5.zonely.geo

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.model.TransitionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class GeofenceRegistrarImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient,
    private val permissionChecker: PermissionChecker
) : GeofenceRegistrar {

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    @SuppressLint("MissingPermission")
    override suspend fun register(zone: GeofenceZone): Result<Unit> {
        if (!permissionChecker.current().canRegisterGeofences) {
            return Result.failure(SecurityException("Insufficient location permission for geofencing"))
        }

        val geofence = zone.toGeofence()
        val request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()

        return runCatching {
            geofencingClient.addGeofences(request, geofencePendingIntent).await()
        }
    }

    override suspend fun unregister(zoneId: String): Result<Unit> {
        return runCatching {
            geofencingClient.removeGeofences(listOf(zoneId)).await()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun reregisterAll(zones: List<GeofenceZone>): Result<Unit> {
        if (zones.isEmpty()) return Result.success(Unit)
        
        if (!permissionChecker.current().canRegisterGeofences) {
            return Result.failure(SecurityException("Insufficient location permission for geofencing"))
        }

        val requests = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(zones.map { it.toGeofence() })
        }.build()

        return runCatching {
            geofencingClient.addGeofences(requests, geofencePendingIntent).await()
        }
    }

    override suspend fun unregisterAll(): Result<Unit> {
        return runCatching {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
        }
    }

    private fun GeofenceZone.toGeofence(): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(mapTransitionTypes(transitionTypes))
            // setNotificationResponsiveness around 5000ms for development.
            // Production should be higher for battery.
            .setNotificationResponsiveness(5000)
            .build()
    }

    /**
     * Pure function for transition-mask conversion, extracted for unit testing.
     */
    fun mapTransitionTypes(types: Set<TransitionType>): Int {
        var mask = 0
        if (types.contains(TransitionType.ENTER)) mask = mask or Geofence.GEOFENCE_TRANSITION_ENTER
        if (types.contains(TransitionType.EXIT)) mask = mask or Geofence.GEOFENCE_TRANSITION_EXIT
        if (types.contains(TransitionType.DWELL)) mask = mask or Geofence.GEOFENCE_TRANSITION_DWELL
        return mask
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWith(Result.failure(it)) }
        }
}
