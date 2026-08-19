package com.group5.zonely.geo.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.geo.LocationProvider
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.geo.FusedLocationProviderImpl
import com.group5.zonely.geo.GeofenceRegistrarImpl
import com.group5.zonely.geo.PermissionCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeoModule {

    @Binds
    @Singleton
    abstract fun bindGeofenceRegistrar(impl: GeofenceRegistrarImpl): GeofenceRegistrar

    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: FusedLocationProviderImpl): LocationProvider

    @Binds
    @Singleton
    abstract fun bindPermissionChecker(impl: PermissionCheckerImpl): PermissionChecker

    companion object {
        @Provides
        @Singleton
        fun provideGeofencingClient(@ApplicationContext context: Context): GeofencingClient {
            return LocationServices.getGeofencingClient(context)
        }

        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(context)
        }
    }
}
