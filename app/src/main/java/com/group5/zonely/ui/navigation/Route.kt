package com.group5.zonely.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Onboarding : Route
    @Serializable data object Home : Route
    @Serializable data class ZoneEditor(val zoneId: String? = null) : Route   // S1
    @Serializable data object History : Route
    @Serializable data object Settings : Route                               // S1
    @Serializable data object About : Route
}
