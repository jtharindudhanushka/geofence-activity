package com.group5.zonely.geo

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.group5.zonely.domain.geo.LocationProvider
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.SimpleLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FusedLocationProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient,
    private val permissionChecker: PermissionChecker
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): Result<SimpleLocation> {
        if (!permissionChecker.current().canUseForegroundLocation) {
            return Result.failure(SecurityException("Missing location permission"))
        }

        val cts = CancellationTokenSource()
        return runCatching {
            val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            if (location != null) {
                SimpleLocation(
                    location.latitude,
                    location.longitude,
                    location.accuracy,
                    location.time
                )
            } else {
                // Fallback to last location
                val lastLocation = client.lastLocation.await()
                if (lastLocation != null) {
                    SimpleLocation(
                        lastLocation.latitude,
                        lastLocation.longitude,
                        lastLocation.accuracy,
                        lastLocation.time
                    )
                } else {
                    throw Exception("Could not retrieve location")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun locationUpdates(intervalMillis: Long): Flow<SimpleLocation> = callbackFlow {
        if (!permissionChecker.current().canUseForegroundLocation) {
            close(SecurityException("Missing location permission"))
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(
                        SimpleLocation(
                            location.latitude,
                            location.longitude,
                            location.accuracy,
                            location.time
                        )
                    )
                }
            }
        }

        client.requestLocationUpdates(request, callback, context.mainLooper)

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T? =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resume(null) }
            addOnCanceledListener { cont.resume(null) }
        }
}
