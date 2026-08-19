package com.group5.zonely.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group5.zonely.domain.model.GeofenceEvent
import com.group5.zonely.domain.repository.GeofenceEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HistoryUiState(
    val groupedEvents: Map<String, List<GeofenceEvent>> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val eventRepository: GeofenceEventRepository
) : ViewModel() {

    private val dayFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = eventRepository.observeEvents()
        .map { events ->
            val grouped = events.groupBy { event ->
                dayFormatter.format(Date(event.occurredAt))
            }
            HistoryUiState(groupedEvents = grouped)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState(isLoading = true))
}
