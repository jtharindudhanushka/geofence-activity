package com.group5.zonely.geo.di

import com.group5.zonely.domain.geo.GeofenceSimulator
import com.group5.zonely.geo.GeofenceSimulatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugGeoModule {
    @Binds
    @Singleton
    abstract fun bindGeofenceSimulator(impl: GeofenceSimulatorImpl): GeofenceSimulator
}
