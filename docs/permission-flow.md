# Zonely Permission Flow

This document outlines the multi-step permission flow required for Zonely to function correctly, specifically focusing on location and notification permissions across different Android API levels.

## Why are these permissions needed?

1.  **Foreground Location (Fine & Coarse):** Essential for determining the user's current position relative to defined geofences when the app is active.
2.  **Background Location:** Mandatory for geofences to trigger (ENTER/EXIT events) when the app is in the background or closed. Without this, the core value proposition of Zonely is lost.
3.  **Notifications:** Required on Android 13+ (API 33) to alert the user when they cross a geofence boundary.

---

## The Flow

The flow is designed to "explain before asking," providing a clear rationale to the user before the system permission dialog appears.

### Step 1: Welcome
*   **Purpose:** Introduce Zonely's core features.
*   **Action:** None. User clicks "Get Started".

### Step 2: Foreground Location
*   **Requirement:** `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`.
*   **Rationale:** "Zonely needs to know where you are to alert you when you enter or exit your zones."
*   **Handling:**
    *   **Granted:** Move to Step 3.
    *   **Denied Once:** Show rationale and a "Retry" button.
    *   **Permanently Denied:** Show a link to system settings.

### Step 3: Background Location (API 29+)
*   **Requirement:** `ACCESS_BACKGROUND_LOCATION`.
*   **Constraint:** This **must** be requested separately from foreground location. Android will silently deny background location if requested simultaneously with foreground location.
*   **Rationale:** "To alert you even when the app is closed, Zonely needs 'Always Allow' location access."
*   **API Differences:**
    *   **API 29:** System dialog is shown.
    *   **API 30+:** No system dialog is shown. The user must be directed to the application's details page in system settings, where they must manually select **"Allow all the time"**.
*   **Handling:**
    *   **Granted:** Move to Step 4.
    *   **Denied:** Show rationale or settings link depending on the state.

### Step 4: Notifications (API 33+)
*   **Requirement:** `POST_NOTIFICATIONS`.
*   **Rationale:** "Zonely uses notifications to alert you immediately when a zone transition occurs."
*   **Handling:**
    *   **Granted/Skipped (< API 33):** Move to Step 5.
    *   **Denied:** User can continue with "limited functionality" (no alerts, only in-app history).

### Step 5: Location Services
*   **Requirement:** System-wide Location Services (GPS) must be enabled.
*   **Action:** If disabled, prompt the user to enable it and provide a deep link to Location Settings.

---

## Denial Logic & Recovery

We follow a "Never a dead end" policy:

*   **First Denial:** We show a custom rationale screen explaining *why* the permission is needed for the app to function, then offer a "Try Again" button which re-triggers the system prompt.
*   **Permanent Denial:** If the user denies twice (or checks "Don't ask again"), `shouldShowRequestPermissionRationale` returns false. We then hide the retry button and show a "Open Settings" button that deep-links to the app's system settings page.
*   **Exit Hatch:** Every screen includes a "Continue with limited functionality" option. This allows the user to explore the app even if they aren't ready to grant all permissions yet.

## Automatic UI Updates

The onboarding UI uses a `LifecycleEventObserver`. When the user returns to the app from the system settings (after manually granting a permission), the app detects the `ON_RESUME` event, refreshes the `PermissionChecker` state, and automatically advances the flow if the required permission is now present.
