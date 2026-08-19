@file:OptIn(ExperimentalMaterial3Api::class)

package com.group5.zonely.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.model.ThemeMode
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.ui.components.*
import com.group5.zonely.ui.theme.ZonelyTheme
import com.group5.zonely.ui.theme.LocalSpacing

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onToggleZone: (String, Boolean) -> Unit,
    onSimulateTransition: (String, TransitionType) -> Unit,
    onAddZone: () -> Unit,
    onEditZone: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ZonelyTopAppBar(
                title = "Zonely",
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                showMenu = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddZone) {
                Icon(Icons.Default.Add, contentDescription = "Add Zone")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                item {
                    HeroStatusCard(
                        activeZone = uiState.activeZone,
                        distanceToEdge = uiState.distanceToEdge,
                        accuracy = uiState.currentLocation?.accuracyMeters ?: 0f
                    )
                }

                if (uiState.permissionState?.canRegisterGeofences == false) {
                    item {
                        PermissionBanner(state = uiState.permissionState)
                    }
                }

                item {
                    Text(
                        text = "ACTIVE ZONES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (uiState.zones.isEmpty()) {
                    item {
                        Text(
                            text = "No zones defined. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.zones) { zone ->
                        ZoneCard(
                            zone = zone,
                            onActiveChanged = { active -> onToggleZone(zone.id, active) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (uiState.recentEvents.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "RECENT ACTIVITY",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = onOpenHistory) {
                                Text("See all")
                            }
                        }
                    }

                    items(uiState.recentEvents) { event ->
                        // Simplified event row for Home
                        ListItem(
                            headlineContent = { Text("${event.transition.name} ${event.zoneName}") },
                            supportingContent = { Text("${event.occurredAt}") }, // Format time in real app
                            leadingContent = { Icon(Icons.Default.MoreVert, contentDescription = null) } // Placeholder icon
                        )
                    }
                }

                // Debug Simulator
                item {
                    DebugSimulator(
                        zones = uiState.zones,
                        onSimulate = onSimulateTransition
                    )
                }
            }
        }
    }
}

@Preview(name = "Home - Loading", showBackground = true)
@Composable
private fun HomeLoadingPreview() {
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(isLoading = true),
            onToggleZone = { _, _ -> },
            onSimulateTransition = { _, _ -> },
            onAddZone = {},
            onEditZone = {},
            onOpenHistory = {},
            onOpenSettings = {}
        )
    }
}

@Preview(name = "Home - Empty", showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, zones = emptyList()),
            onToggleZone = { _, _ -> },
            onSimulateTransition = { _, _ -> },
            onAddZone = {},
            onEditZone = {},
            onOpenHistory = {},
            onOpenSettings = {}
        )
    }
}

@Preview(name = "Home - With Zones", showBackground = true)
@Composable
private fun HomeWithZonesPreview() {
    val zones = listOf(
        GeofenceZone("1", "Home", 0.0, 0.0, 200f, colorArgb = 0xFFE91E63.toInt(), createdAt = 0, updatedAt = 0),
        GeofenceZone("2", "Campus", 0.0, 0.0, 300f, colorArgb = 0xFF2196F3.toInt(), createdAt = 0, updatedAt = 0)
    )
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, zones = zones),
            onToggleZone = { _, _ -> },
            onSimulateTransition = { _, _ -> },
            onAddZone = {},
            onEditZone = {},
            onOpenHistory = {},
            onOpenSettings = {}
        )
    }
}

@Composable
fun HeroStatusCard(
    activeZone: GeofenceZone?,
    distanceToEdge: Float?,
    accuracy: Float,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val containerColor = if (activeZone != null) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (activeZone != null) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg)
        ) {
            Text(
                text = if (activeZone != null) "Inside ${activeZone.name}" else "Outside all zones",
                style = MaterialTheme.typography.headlineLarge
            )
            if (activeZone != null) {
                Text(
                    text = "Radius: ${activeZone.radiusMeters.toInt()} m",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (distanceToEdge != null) {
                Text(
                    text = "${distanceToEdge.toInt()} m from the edge",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "accuracy ±${accuracy.toInt()} m",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = spacing.sm)
            )
        }
    }
}

@Composable
fun DebugSimulator(
    zones: List<GeofenceZone>,
    onSimulate: (String, TransitionType) -> Unit
) {
    val spacing = LocalSpacing.current
    if (zones.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = spacing.xl)) {
            Text(text = "DEBUG: SIMULATE TRANSITION", style = MaterialTheme.typography.labelSmall)
            zones.forEach { zone ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    TextButton(onClick = { onSimulate(zone.id, TransitionType.ENTER) }) {
                        Text("ENTER ${zone.name}")
                    }
                    TextButton(onClick = { onSimulate(zone.id, TransitionType.EXIT) }) {
                        Text("EXIT ${zone.name}")
                    }
                }
            }
        }
    }
}
