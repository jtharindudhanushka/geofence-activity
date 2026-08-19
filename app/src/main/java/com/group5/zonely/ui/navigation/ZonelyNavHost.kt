package com.group5.zonely.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.group5.zonely.domain.repository.SettingsRepository
import com.group5.zonely.ui.about.AboutRoute
import com.group5.zonely.ui.history.HistoryRoute
import com.group5.zonely.ui.home.HomeRoute
import com.group5.zonely.ui.onboarding.OnboardingRoute
import com.group5.zonely.ui.settings.SettingsRoute
import com.group5.zonely.ui.zoneeditor.ZoneEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val startDestination = settingsRepository.settings
        .map { if (it.onboardingCompleted) Route.Home else Route.Onboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@Composable
fun ZonelyNavHost() {
    val navController = rememberNavController()
    val viewModel: NavViewModel = hiltViewModel()
    val startDestination by viewModel.startDestination.collectAsState()

    if (startDestination == null) return // Or splash screen

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable<Route.Onboarding> {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Home> {
            HomeRoute(
                onAddZone = { navController.navigate(Route.ZoneEditor()) },
                onEditZone = { id -> navController.navigate(Route.ZoneEditor(id)) },
                onOpenHistory = { navController.navigate(Route.History) },
                onOpenSettings = { navController.navigate(Route.Settings) }
            )
        }
        composable<Route.ZoneEditor> { backStackEntry ->
            val route: Route.ZoneEditor = backStackEntry.toRoute()
            ZoneEditorRoute(
                zoneId = route.zoneId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.History> {
            HistoryRoute(onBack = { navController.popBackStack() })
        }
        composable<Route.Settings> {
            SettingsRoute(
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(Route.About) }
            )
        }
        composable<Route.About> {
            AboutRoute(onBack = { navController.popBackStack() })
        }
    }
}
