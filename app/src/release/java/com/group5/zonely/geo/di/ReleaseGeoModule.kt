package com.group5.zonely.geo.di

import com.group5.zonely.domain.geo.GeofenceSimulator
import com.group5.zonely.geo.GeofenceSimulatorNoOp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReleaseGeoModule {
    @Binds
    @Singleton
    abstract fun bindGeofenceSimulator(impl: GeofenceSimulatorNoOp): GeofenceSimulator
}
