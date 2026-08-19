package com.group5.zonely.ui.onboarding

import com.group5.zonely.domain.model.PermissionState
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingStepTest {

    @Test
    fun `starts with welcome when not acknowledged`() {
        val state = PermissionState(false, false, false, false, false)
        val step = OnboardingViewModel.resolveCurrentStep(state, apiLevel = 33, welcomeAcknowledged = false)
        assertEquals(OnboardingStep.WELCOME, step)
    }

    @Test
    fun `shows foreground location after welcome`() {
        val state = PermissionState(false, false, false, false, false)
        val step = OnboardingViewModel.resolveCurrentStep(state, apiLevel = 33, welcomeAcknowledged = true)
        assertEquals(OnboardingStep.FOREGROUND_LOCATION, step)
    }

    @Test
    fun `skips background location on API level less than 29`() {
        val state = PermissionState(true, true, false, false, true)
        val step = OnboardingViewModel.resolveCurrentStep(state, apiLevel = 28, welcomeAcknowledged = true)
        assertEquals(OnboardingStep.COMPLETED, step) // Since notifications only API 33+
    }

    @Test
    fun `shows notifications step on API 33`() {
        val state = PermissionState(true, true, true, false, true)
        val step = OnboardingViewModel.resolveCurrentStep(state, apiLevel = 33, welcomeAcknowledged = true)
        assertEquals(OnboardingStep.NOTIFICATIONS, step)
    }

    @Test
    fun `shows background location on API 29+`() {
        val state = PermissionState(true, true, false, false, true)
        val step = OnboardingViewModel.resolveCurrentStep(state, apiLevel = 29, welcomeAcknowledged = true)
        assertEquals(OnboardingStep.BACKGROUND_LOCATION, step)
    }

    @Test
    fun `advances to completed when all granted`() {
        val state = PermissionState(true, true, true, true, true)
        val step = OnboardingViewModel.resolveCurrentStep(state, apiLevel = 33, welcomeAcknowledged = true)
        assertEquals(OnboardingStep.COMPLETED, step)
    }
}
