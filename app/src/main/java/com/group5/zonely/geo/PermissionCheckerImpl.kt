package com.group5.zonely.geo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.PermissionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionCheckerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionChecker {

    private val _state = MutableStateFlow(getPermissionState())

    override fun current(): PermissionState = _state.value

    override fun observe(): Flow<PermissionState> = _state.asStateFlow()

    override fun refresh() {
        _state.value = getPermissionState()
    }

    private fun getPermissionState(): PermissionState {
        val fineLocation = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        
        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true // Always granted if granted foreground before API 29
        }

        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return PermissionState(
            fineLocationGranted = fineLocation,
            coarseLocationGranted = coarseLocation,
            backgroundLocationGranted = backgroundLocation,
            notificationsGranted = notifications,
            locationServicesEnabled = locationEnabled
        )
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
