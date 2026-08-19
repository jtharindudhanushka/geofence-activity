package com.group5.zonely.core.di

import com.group5.zonely.data.repository.InMemoryGeofenceEventRepository
import com.group5.zonely.data.repository.InMemoryGeofenceZoneRepository
import com.group5.zonely.data.repository.InMemorySettingsRepository
import com.group5.zonely.domain.repository.GeofenceEventRepository
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import com.group5.zonely.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGeofenceZoneRepository(
        impl: InMemoryGeofenceZoneRepository
    ): GeofenceZoneRepository

    @Binds
    @Singleton
    abstract fun bindGeofenceEventRepository(
        impl: InMemoryGeofenceEventRepository
    ): GeofenceEventRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: InMemorySettingsRepository
    ): SettingsRepository
}
