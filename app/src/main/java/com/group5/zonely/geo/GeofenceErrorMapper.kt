package com.group5.zonely.geo

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.group5.zonely.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceErrorMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> 
                context.getString(R.string.error_geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> 
                context.getString(R.string.error_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> 
                context.getString(R.string.error_too_many_pending_intents)
            GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION -> 
                context.getString(R.string.error_insufficient_location_permission)
            else -> context.getString(R.string.error_geofence_unknown)
        }
    }
}
