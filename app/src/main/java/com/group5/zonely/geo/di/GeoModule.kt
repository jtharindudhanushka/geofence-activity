package com.group5.zonely.geo.di

import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.geo.LocationProvider
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.PermissionState
import com.group5.zonely.domain.model.SimpleLocation
import com.group5.zonely.domain.model.GeofenceZone
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

// OWNER: Dev B - replace these stubs with real implementations.

@Module
@InstallIn(SingletonComponent::class)
object GeoModule {

    @Provides
    @Singleton
    fun provideGeofenceRegistrar(): GeofenceRegistrar = object : GeofenceRegistrar {
        override suspend fun register(zone: GeofenceZone): Result<Unit> = Result.success(Unit)
        override suspend fun unregister(zoneId: String): Result<Unit> = Result.success(Unit)
        override suspend fun reregisterAll(zones: List<GeofenceZone>): Result<Unit> = Result.success(Unit)
        override suspend fun unregisterAll(): Result<Unit> = Result.success(Unit)
    }

    @Provides
    @Singleton
    fun provideLocationProvider(): LocationProvider = object : LocationProvider {
        override suspend fun currentLocation(): Result<SimpleLocation> = 
            Result.success(SimpleLocation(0.0, 0.0, 0f, System.currentTimeMillis()))
        override fun locationUpdates(intervalMillis: Long): Flow<SimpleLocation> = flowOf()
    }

    @Provides
    @Singleton
    fun providePermissionChecker(): PermissionChecker = object : PermissionChecker {
        private val state = MutableStateFlow(PermissionState(false, false, false, false, false))
        override fun current(): PermissionState = state.value
        override fun observe(): Flow<PermissionState> = state
        override fun refresh() {}
    }
}
