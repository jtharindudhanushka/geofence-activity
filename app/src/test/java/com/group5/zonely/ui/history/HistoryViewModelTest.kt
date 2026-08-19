package com.group5.zonely.ui.history

import app.cash.turbine.test
import com.group5.zonely.domain.model.GeofenceEvent
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.domain.repository.GeofenceEventRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class HistoryViewModelTest {

    private val repository = mockk<GeofenceEventRepository>()
    private lateinit var viewModel: HistoryViewModel

    @Test
    fun `groups events by day correctly`() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2024, 0, 1, 10, 0) // Jan 1, 2024
        val time1 = calendar.timeInMillis
        
        calendar.set(2024, 0, 1, 11, 0)
        val time2 = calendar.timeInMillis
        
        calendar.set(2024, 0, 2, 10, 0) // Jan 2, 2024
        val time3 = calendar.timeInMillis

        val events = listOf(
            GeofenceEvent(1, "z1", "Home", TransitionType.ENTER, time1, null, null, null),
            GeofenceEvent(2, "z1", "Home", TransitionType.EXIT, time2, null, null, null),
            GeofenceEvent(3, "z1", "Home", TransitionType.ENTER, time3, null, null, null)
        )

        every { repository.observeEvents() } returns flowOf(events)
        
        viewModel = HistoryViewModel(repository)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.groupedEvents.size)
            assertTrue(state.groupedEvents.any { it.key.contains("1") })
            assertTrue(state.groupedEvents.any { it.key.contains("2") })
            assertEquals(2, state.groupedEvents.entries.find { it.key.contains("1") }?.value?.size)
            assertEquals(1, state.groupedEvents.entries.find { it.key.contains("2") }?.value?.size)
        }
    }
}
