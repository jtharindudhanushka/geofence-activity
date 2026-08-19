package com.group5.zonely.domain.model

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val onboardingCompleted: Boolean = false,
)
