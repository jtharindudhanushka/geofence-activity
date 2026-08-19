package com.group5.zonely.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeRoute(
    onAddZone: () -> Unit,
    onEditZone: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onToggleZone = viewModel::toggleZone,
        onSimulateTransition = viewModel::simulateTransition,
        onAddZone = onAddZone,
        onEditZone = onEditZone,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings
    )
}
