# Zonely

Zonely lets a user define a circular location zone and get alerted the moment they enter or exit it, with a timestamped history of every crossing.

## Architecture

Zonely follows the MVVM architecture with unidirectional data flow, organized into `ui`, `domain`, and `data` packages.

- **UI**: Jetpack Compose using Material 3 Expressive.
- **Domain**: Pure Kotlin models, repository interfaces, and use cases.
- **Data**: Repository implementations. S0 uses an in-memory data layer. S1 will introduce Room and DataStore.
- **DI**: Hilt for dependency injection.
- **Location**: Google Play Services Geofencing and Location APIs.

## Module Map

- `com.group5.zonely.core`: Common utilities, DI modules, and formatters.
- `com.group5.zonely.domain`: Business logic, models, and shared interfaces.
- `com.group5.zonely.data`: Data sources and repository implementations.
- `com.group5.zonely.geo`: Geofencing registration and location tracking.
- `com.group5.zonely.notification`: Local notifications for geofence transitions.
- `com.group5.zonely.ui`: Compose-based UI, navigation, and theme.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- API 26 (Android 8.0) or higher.
- Google Play Services on the device/emulator.

### Test Gate

Run the following command before every push to ensure code quality:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

## Documentation

- [AGENTS.md](AGENTS.md): Project contracts and workflow.
- [DESIGN.md](DESIGN.md): Design system and UX specification.
- `docs/permission-flow.md`: Explanation of the multi-step permission flow.
