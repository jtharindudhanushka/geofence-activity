package com.group5.zonely.geo

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.group5.zonely.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeofenceErrorMapperTest {

    private lateinit var context: Context
    private lateinit var mapper: GeofenceErrorMapper

    @Before
    fun setup() {
        context = mockk()
        mapper = GeofenceErrorMapper(context)
        
        every { context.getString(R.string.error_geofence_not_available) } returns "Not available"
        every { context.getString(R.string.error_too_many_geofences) } returns "Too many"
        every { context.getString(R.string.error_too_many_pending_intents) } returns "Too many pending"
        every { context.getString(R.string.error_insufficient_location_permission) } returns "Insufficient permission"
        every { context.getString(R.string.error_geofence_unknown) } returns "Unknown"
    }

    @Test
    fun `getErrorMessage returns correct string for GEOFENCE_NOT_AVAILABLE`() {
        assertEquals("Not available", mapper.getErrorMessage(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE))
    }

    @Test
    fun `getErrorMessage returns correct string for GEOFENCE_TOO_MANY_GEOFENCES`() {
        assertEquals("Too many", mapper.getErrorMessage(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES))
    }

    @Test
    fun `getErrorMessage returns correct string for unknown code`() {
        assertEquals("Unknown", mapper.getErrorMessage(999))
    }
}
