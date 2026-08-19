package com.group5.zonely.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryGeofenceZoneRepositoryTest {

    private val repository = InMemoryGeofenceZoneRepository()

    @Test
    fun `observeZones emits initial seeded zones`() = runTest {
        repository.observeZones().test {
            val zones = awaitItem()
            assertEquals(2, zones.size)
            assertTrue(zones.any { it.name == "Home Zone" })
            assertTrue(zones.any { it.name == "Campus" })
        }
    }

    @Test
    fun `setActive updates zone status`() = runTest {
        val zoneId = "home-zone"
        repository.setActive(zoneId, false)
        
        val zone = repository.getZone(zoneId)
        assertEquals(false, zone?.isActive)
    }
}
