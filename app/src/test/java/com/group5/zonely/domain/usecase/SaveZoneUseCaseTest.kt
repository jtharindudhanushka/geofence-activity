package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveZoneUseCaseTest {

    private val repository = mockk<GeofenceZoneRepository>()
    private val useCase = SaveZoneUseCase(repository)

    @Test
    fun `invoke returns failure when name is blank`() = runTest {
        val zone = createZone(name = "")
        val result = useCase(zone)
        assertTrue(result.isFailure)
        assertEquals("Name cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when radius is less than 50`() = runTest {
        val zone = createZone(radius = 49f)
        val result = useCase(zone)
        assertTrue(result.isFailure)
        assertEquals("Radius too small", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns success and calls repository when valid`() = runTest {
        val zone = createZone()
        coEvery { repository.upsert(any()) } returns Unit
        
        val result = useCase(zone)
        
        assertTrue(result.isSuccess)
        coVerify { repository.upsert(zone) }
    }

    private fun createZone(name: String = "Test", radius: Float = 100f) = GeofenceZone(
        id = "1",
        name = name,
        latitude = 0.0,
        longitude = 0.0,
        radiusMeters = radius,
        colorArgb = 0,
        createdAt = 0,
        updatedAt = 0
    )

    private fun assertEquals(expected: Any?, actual: Any?) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}
