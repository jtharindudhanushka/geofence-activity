package com.group5.zonely.ui.history

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.group5.zonely.R
import com.group5.zonely.domain.model.GeofenceEvent
import com.group5.zonely.domain.model.TransitionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.groupedEvents.isEmpty()) {
            EmptyHistory(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.groupedEvents.forEach { (day, events) ->
                    stickyHeader {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(events) { event ->
                        HistoryRow(event)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRow(event: GeofenceEvent) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        event.occurredAt,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )

    ListItem(
        headlineContent = {
            Text(
                text = event.zoneName,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = if (event.transition == TransitionType.ENTER) 
                        stringResource(R.string.history_entered, event.zoneName)
                    else 
                        stringResource(R.string.history_left, event.zoneName),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${relativeTime} • ${timeFormatter.format(Date(event.occurredAt))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.latitude != null && event.longitude != null) {
                    Text(
                        text = "%.5f, %.5f (±%.1fm)".format(event.latitude, event.longitude, event.accuracyMeters ?: 0f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = MaterialTheme.shapes.full,
                color = MaterialTheme.colorScheme.primary
            ) {}
        },
        trailingContent = {
            Icon(
                imageVector = if (event.transition == TransitionType.ENTER) Icons.Default.Login else Icons.Default.Logout,
                contentDescription = null,
                tint = if (event.transition == TransitionType.ENTER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    )
}

@Composable
fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.history_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
