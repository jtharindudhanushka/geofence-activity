package com.group5.zonely.domain.model

data class PermissionState(
    val fineLocationGranted: Boolean,
    val coarseLocationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
    val notificationsGranted: Boolean,
    val locationServicesEnabled: Boolean,
) {
    val canUseForegroundLocation get() = fineLocationGranted || coarseLocationGranted
    val canRegisterGeofences get() = fineLocationGranted && backgroundLocationGranted
}
