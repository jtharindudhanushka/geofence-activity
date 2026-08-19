package com.group5.zonely.domain.usecase

import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.domain.repository.GeofenceEventRepository
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordTransitionUseCaseTest {

    private val zoneRepository = mockk<GeofenceZoneRepository>()
    private val eventRepository = mockk<GeofenceEventRepository>()
    private val useCase = RecordTransitionUseCase(zoneRepository, eventRepository)

    @Test
    fun `invoke records event when zone exists`() = runTest {
        val zoneId = "zone-1"
        val zone = GeofenceZone(
            id = zoneId,
            name = "Test Zone",
            latitude = 0.0,
            longitude = 0.0,
            radiusMeters = 100f,
            colorArgb = 0,
            createdAt = 0,
            updatedAt = 0
        )
        
        coEvery { zoneRepository.getZone(zoneId) } returns zone
        coEvery { eventRepository.record(any()) } returns Unit
        
        val result = useCase(zoneId, TransitionType.ENTER)
        
        assertTrue(result.isSuccess)
        coVerify { eventRepository.record(match { it.zoneName == "Test Zone" && it.transition == TransitionType.ENTER }) }
    }

    @Test
    fun `invoke returns failure when zone does not exist`() = runTest {
        coEvery { zoneRepository.getZone(any()) } returns null
        
        val result = useCase("non-existent", TransitionType.ENTER)
        
        assertTrue(result.isFailure)
    }
}
