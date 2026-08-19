package com.group5.zonely.geo

import android.content.Context
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.TransitionType
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeofenceRegistrarImplTest {

    private lateinit var context: Context
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var permissionChecker: PermissionChecker
    private lateinit var registrar: GeofenceRegistrarImpl

    @Before
    fun setup() {
        context = mockk()
        geofencingClient = mockk()
        permissionChecker = mockk()
        registrar = GeofenceRegistrarImpl(context, geofencingClient, permissionChecker)
    }

    @Test
    fun `mapTransitionTypes correctly maps ENTER and EXIT`() {
        val types = setOf(TransitionType.ENTER, TransitionType.EXIT)
        val mask = registrar.mapTransitionTypes(types)
        
        assertEquals(
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT,
            mask
        )
    }

    @Test
    fun `mapTransitionTypes returns 0 for empty set`() {
        val mask = registrar.mapTransitionTypes(emptySet())
        assertEquals(0, mask)
    }

    @Test
    fun `mapTransitionTypes handles DWELL`() {
        val types = setOf(TransitionType.DWELL)
        val mask = registrar.mapTransitionTypes(types)
        assertEquals(Geofence.GEOFENCE_TRANSITION_DWELL, mask)
    }
}
