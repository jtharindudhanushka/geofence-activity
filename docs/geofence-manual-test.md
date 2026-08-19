# Geofence Manual Test Log

## Test Environment
- Device: Emulator (Pixel 6)
- API Level: 33
- Play Services: Available

## Test Steps & Results

### 1. Permission Flow
- **Step**: Install app, walk through onboarding.
- **Expected**: Prompts for Fine Location, then Background Location, then Notifications.
- **Result**: PASS.

### 2. Geofence Registration
- **Step**: Add a zone or use default seeded zones.
- **Expected**: `GeofenceRegistrar` returns success.
- **Result**: PASS.

### 3. ENTER Transition
- **Step**: Use Emulator Extended Controls to set location inside a zone.
- **Expected**: Notification "Entered {Zone}" appears. History entry recorded.
- **Result**: PASS.

### 4. EXIT Transition
- **Step**: Set location outside the zone.
- **Expected**: Notification "Left {Zone}" appears. History entry recorded.
- **Result**: PASS.

### 5. Debug Simulation
- **Step**: Use the "Simulate ENTER" button on Home (Debug build).
- **Expected**: Immediate notification and history record without moving location.
- **Result**: PASS.

### 6. App Kill Survival
- **Step**: Kill app, move location into zone.
- **Expected**: Notification still fires.
- **Result**: PASS.

## Observations
- Transition delay on emulator was ~45 seconds.
- Background location is critical; without "Allow all the time", transitions do not fire when the app is in background.
