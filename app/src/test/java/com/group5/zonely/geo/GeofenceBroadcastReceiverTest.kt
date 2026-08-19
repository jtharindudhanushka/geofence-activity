package com.group5.zonely.geo

import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.group5.zonely.domain.model.TransitionType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class GeofenceBroadcastReceiverTest {

    @Test
    fun `parseGeofencingEvent correctly maps ENTER transition`() {
        val receiver = GeofenceBroadcastReceiver()
        val event = mockk<GeofencingEvent>()
        val geofence = mockk<Geofence>()
        val location = mockk<Location>()

        every { event.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_ENTER
        every { event.triggeringGeofences } returns listOf(geofence)
        every { event.triggeringLocation } returns location
        every { geofence.requestId } returns "zone1"
        every { location.latitude } returns 1.0
        every { location.longitude } returns 2.0
        every { location.accuracy } returns 3.0f

        val result = receiver.parseGeofencingEvent(event)

        assertEquals(1, result.size)
        assertEquals("zone1", result[0].zoneId)
        assertEquals(TransitionType.ENTER, result[0].transition)
        assertEquals(1.0, result[0].latitude)
        assertEquals(2.0, result[0].longitude)
        assertEquals(3.0f, result[0].accuracy)
    }

    @Test
    fun `parseGeofencingEvent returns empty list on invalid transition`() {
        val receiver = GeofenceBroadcastReceiver()
        val event = mockk<GeofencingEvent>()

        every { event.geofenceTransition } returns -1

        val result = receiver.parseGeofencingEvent(event)

        assertEquals(0, result.size)
    }
}
