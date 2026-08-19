package com.group5.zonely.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group5.zonely.domain.geo.GeofenceSimulator
import com.group5.zonely.domain.geo.LocationProvider
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.*
import com.group5.zonely.domain.repository.GeofenceEventRepository
import com.group5.zonely.domain.repository.SettingsRepository
import com.group5.zonely.domain.usecase.ObserveZonesUseCase
import com.group5.zonely.domain.usecase.ToggleZoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class HomeUiState(
    val zones: List<GeofenceZone> = emptyList(),
    val recentEvents: List<GeofenceEvent> = emptyList(),
    val permissionState: PermissionState? = null,
    val currentLocation: SimpleLocation? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val activeZone: GeofenceZone? = zones.firstOrNull { zone ->
        currentLocation?.let { loc ->
            calculateDistance(loc.latitude, loc.longitude, zone.latitude, zone.longitude) <= zone.radiusMeters
        } ?: false
    }

    val distanceToEdge: Float? = activeZone?.let { zone ->
        currentLocation?.let { loc ->
            val dist = calculateDistance(loc.latitude, loc.longitude, zone.latitude, zone.longitude)
            abs(zone.radiusMeters - dist)
        }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeZonesUseCase: ObserveZonesUseCase,
    private val toggleZoneUseCase: ToggleZoneUseCase,
    private val eventRepository: GeofenceEventRepository,
    private val permissionChecker: PermissionChecker,
    private val locationProvider: LocationProvider,
    private val settingsRepository: SettingsRepository,
    private val geofenceSimulator: GeofenceSimulator
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        observeZonesUseCase(),
        eventRepository.observeEvents(limit = 3),
        permissionChecker.observe(),
        locationProvider.locationUpdates()
    ) { zones, events, permissions, location ->
        HomeUiState(
            zones = zones,
            recentEvents = events,
            permissionState = permissions,
            currentLocation = location,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleZone(zoneId: String, active: Boolean) {
        viewModelScope.launch {
            toggleZoneUseCase(zoneId, active)
        }
    }

    fun simulateTransition(zoneId: String, transition: TransitionType) {
        viewModelScope.launch {
            geofenceSimulator.simulateTransition(zoneId, transition)
        }
    }

    fun refreshPermissions() {
        permissionChecker.refresh()
    }
}

// Haversine formula for distance calculation
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (r * c).toFloat()
}
