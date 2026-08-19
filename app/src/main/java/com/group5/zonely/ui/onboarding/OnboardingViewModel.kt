package com.group5.zonely.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group5.zonely.domain.geo.PermissionChecker
import com.group5.zonely.domain.model.PermissionState
import com.group5.zonely.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    FOREGROUND_LOCATION,
    BACKGROUND_LOCATION,
    NOTIFICATIONS,
    LOCATION_SERVICES,
    COMPLETED
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val permissionState: StateFlow<PermissionState> = permissionChecker.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), permissionChecker.current())

    fun refreshPermissions() {
        permissionChecker.refresh()
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(onboardingCompleted = true) }
            onFinished()
        }
    }

    companion object {
        fun resolveCurrentStep(
            state: PermissionState,
            apiLevel: Int,
            welcomeAcknowledged: Boolean
        ): OnboardingStep {
            if (!welcomeAcknowledged) return OnboardingStep.WELCOME
            if (!state.canUseForegroundLocation) return OnboardingStep.FOREGROUND_LOCATION
            if (apiLevel >= 29 && !state.backgroundLocationGranted) return OnboardingStep.BACKGROUND_LOCATION
            if (apiLevel >= 33 && !state.notificationsGranted) return OnboardingStep.NOTIFICATIONS
            if (!state.locationServicesEnabled) return OnboardingStep.LOCATION_SERVICES
            return OnboardingStep.COMPLETED
        }
    }
}
