package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.geo.GeofenceRegistrar
import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleZoneUseCaseTest {

    private val repository = mockk<GeofenceZoneRepository>()
    private val registrar = mockk<GeofenceRegistrar>()
    private val useCase = ToggleZoneUseCase(repository, registrar)

    @Test
    fun `invoke registers zone when toggled on`() = runTest {
        val zoneId = "1"
        val zone = createZone(zoneId)
        coEvery { repository.setActive(zoneId, true) } returns Unit
        coEvery { repository.getZone(zoneId) } returns zone
        coEvery { registrar.register(zone) } returns Result.success(Unit)
        
        val result = useCase(zoneId, true)
        
        assertTrue(result.isSuccess)
        coVerify { registrar.register(zone) }
    }

    @Test
    fun `invoke unregisters zone when toggled off`() = runTest {
        val zoneId = "1"
        val zone = createZone(zoneId)
        coEvery { repository.setActive(zoneId, false) } returns Unit
        coEvery { repository.getZone(zoneId) } returns zone
        coEvery { registrar.unregister(zoneId) } returns Result.success(Unit)
        
        val result = useCase(zoneId, false)
        
        assertTrue(result.isSuccess)
        coVerify { registrar.unregister(zoneId) }
    }

    private fun createZone(id: String) = GeofenceZone(
        id = id,
        name = "Test",
        latitude = 0.0,
        longitude = 0.0,
        radiusMeters = 100f,
        colorArgb = 0,
        createdAt = 0,
        updatedAt = 0
    )
}
