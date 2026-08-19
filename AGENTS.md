# AGENTS.md

Operating contract for every human and every AI agent working in this repo.
**Read this file and `DESIGN.md` before writing a single line of code.**

Project: **Zonely** (Group 5, Android Location Services activity)
Application ID: `com.group5.zonely`
Repo: `https://github.com/jtharindudhanushka/geofence-activity`

---

## 1. What we are building

Zonely lets a user define a circular location zone and get alerted the moment they enter or exit it, with a timestamped history of every crossing.

Team: 4 developers building in parallel (Dev A, B, C, D) plus Dev E on documentation, demo and merge refereeing.

---

## 2. Scope tiers: build S0 first

The activity sheet gives 60 minutes of build time. **S0 is the graded requirement set. Nothing in S1 or S2 gets started until S0 is merged and demoable.** If you are running late, you cut from the bottom, never from the top.

### S0 - SRS baseline (must ship, this is the demo)

Traceability against the Group 5 activity sheet:

| SRS ref | Requirement | Owner | Status gate |
|---|---|---|---|
| R1 | Request location permission at runtime, plus background location on API 29+ | D (UI) + B (checking) | Fresh install walks the flow |
| R2 | Define one geofence with a fixed latitude, longitude and radius | A (default zone) | Zone visible on Home |
| R3 | Register it with `GeofencingClient` using a `PendingIntent` | B | Registration returns success |
| R4 | Handle ENTER and EXIT transitions, show a Toast or notification for each | B | Both fire on emulator |
| R5 | Clearly label on screen which geofence is active (location + radius) | C | Home hero card |
| R6 (stretch) | In-app history list of enter/exit events with timestamps | D | List populates live |
| R7 (stretch) | Support a second geofence and identify which one triggered | A + B | Two zones, correct name in alert |
| NFR1 | Handles permission denial without crashing | D | Denial paths tested |
| NFR2 | Material 3 Expressive theming, light and dark | C | Both render |
| NFR3 | Every member can explain the permission flow | E | `docs/permission-flow.md` |

**S0 explicitly excludes:** Room, DataStore, Google Maps, map picker, settings screen, boot re-registration, DWELL, filters, CSV export. Zones live in an in-memory source with two hardcoded defaults. Events live in an in-memory ring buffer.

### S1 - Full featured layer (only after S0 is merged)

Room persistence for zones and events, DataStore settings, Google Maps zone editor with radius circle, create/edit/delete zones, per-zone active toggle, DWELL transitions, boot re-registration, history filters, settings screen, notification deep link into History.

### S2 - Polish

Theme mode and dynamic colour toggles, distance units, CSV export, foreground service, swipe-to-delete with undo, shape morphing interactions, animated transitions.

**Why this shape:** the repository interfaces in Section 6 are identical in S0 and S1. S0 backs them with in-memory implementations, S1 swaps in Room and DataStore behind the same interfaces. No UI or geofencing code changes when that swap happens.

---

## 3. Stack (locked, do not deviate)

| Concern | Choice |
|---|---|
| Language | Kotlin 2.x, JDK 17 |
| UI | Jetpack Compose |
| Design system | Material 3 **Expressive**, `androidx.compose.material3:material3:1.5.0-alpha24` or newer alpha, version-overridden above the Compose BOM |
| Architecture | MVVM with unidirectional data flow, `ui` / `domain` / `data` packages |
| DI | Hilt |
| Async | Coroutines, `Flow`, `StateFlow` for UI state |
| Location | `com.google.android.gms:play-services-location:21.4.0` |
| Navigation | navigation-compose, type-safe routes via kotlinx-serialization |
| Persistence (S1) | Room, DataStore Preferences |
| Maps (S1) | `com.google.maps.android:maps-compose` |
| Testing | JUnit4, MockK, Turbine, `kotlinx-coroutines-test`, Compose UI test |
| SDK | `minSdk = 26`, compileSdk / targetSdk = latest stable installed (36 or newer) |

All versions live in `gradle/libs.versions.toml`. Only Dev A changes that file. If you need a dependency, ask A.

### Material 3 Expressive rules

- Expressive APIs are in the `1.5.0-alphaXX` line. Stable `1.4.x` is baseline M3 and will not compile expressive components.
- Opt in per file: `@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)`.
- Theme entry point is `MaterialExpressiveTheme(colorScheme, motionScheme, shapes, typography)`.
- Alpha APIs move between releases. **If a symbol does not resolve, do not downgrade the project version.** Ctrl+click into the library, confirm the real API surface, and if the component is genuinely absent fall back to the closest stable M3 component with a `// TODO(M3E):` comment naming what you wanted.
- Component usage guidance lives in `DESIGN.md`.

---

## 4. Package structure and ownership

Single Gradle module `:app`. **The owner column is binding.**

```
com.group5.zonely
├── ZonelyApp.kt / MainActivity.kt                              A
├── core/common, core/di, core/util                             A
├── domain/model, domain/repository, domain/geo, domain/usecase  A
├── data/                (S0: in-memory. S1: Room + DataStore)   A
├── geo/                 registrar, receivers, permission check  B
├── notification/                                               B
└── ui/
    ├── theme/  components/                                     C
    ├── navigation/                                             A
    ├── home/  zoneeditor/                                      C
    └── onboarding/  history/  settings/  about/                D
```

### Shared-file rules

- `AndroidManifest.xml`: A creates it with all permissions. B adds only receiver entries, inside the commented region A leaves at the bottom of `<application>`. Nobody else touches it.
- `res/values/strings.xml`: everyone appends **at the bottom** inside their own comment block (`<!-- ===== DEV C ===== -->`). Never reorder or reformat this file. It is the number one conflict source.
- `gradle/libs.versions.toml` and `app/build.gradle.kts`: Dev A only.
- If a task requires editing a file you do not own, **stop and report it in the group chat.** Do not edit it.

---

## 5. Order of work

1. **Dev A pushes the skeleton and all contracts first.** B, C and D are blocked until it lands.
2. While waiting, B, C and D stub the contracts locally so they can compile. Delete the stubs and rebase on `main` the moment A's PR merges. **Never push a local copy of another dev's file.**
3. **Dev C pushes the theme second.** D cannot style anything until `ZonelyTheme` exists.
4. Everyone else works in parallel from there.

---

## 6. Contracts

These are the interfaces between the four workstreams. **Created by Dev A. Do not rename or change signatures unilaterally.**

```kotlin
// ---------- domain/model ----------
enum class TransitionType { ENTER, EXIT, DWELL }        // S0 uses ENTER and EXIT only
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class DistanceUnit { METRIC, IMPERIAL }

data class GeofenceZone(
    val id: String,                  // UUID string; also the geofence requestId
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val transitionTypes: Set<TransitionType> = setOf(TransitionType.ENTER, TransitionType.EXIT),
    val loiteringDelayMillis: Int = 60_000,
    val isActive: Boolean = true,
    val colorArgb: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

data class GeofenceEvent(
    val id: Long = 0L,
    val zoneId: String,
    val zoneName: String,
    val transition: TransitionType,
    val occurredAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
)

data class SimpleLocation(
    val latitude: Double, val longitude: Double,
    val accuracyMeters: Float, val timeMillis: Long,
)

data class PermissionState(
    val fineLocationGranted: Boolean,
    val coarseLocationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
    val notificationsGranted: Boolean,
    val locationServicesEnabled: Boolean,
) {
    val canUseForegroundLocation get() = fineLocationGranted || coarseLocationGranted
    val canRegisterGeofences get() = fineLocationGranted && backgroundLocationGranted
}

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val onboardingCompleted: Boolean = false,
)

// ---------- domain/repository ----------
interface GeofenceZoneRepository {
    fun observeZones(): Flow<List<GeofenceZone>>
    fun observeZone(id: String): Flow<GeofenceZone?>
    suspend fun getZone(id: String): GeofenceZone?
    suspend fun getActiveZones(): List<GeofenceZone>
    suspend fun upsert(zone: GeofenceZone)          // S1
    suspend fun delete(id: String)                  // S1
    suspend fun setActive(id: String, active: Boolean)
}

interface GeofenceEventRepository {
    fun observeEvents(limit: Int = 200): Flow<List<GeofenceEvent>>
    fun observeEventsForZone(zoneId: String): Flow<List<GeofenceEvent>>
    suspend fun record(event: GeofenceEvent)
    suspend fun clearAll()
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

// ---------- domain/geo (declared by A, implemented by B) ----------
interface GeofenceRegistrar {
    suspend fun register(zone: GeofenceZone): Result<Unit>
    suspend fun unregister(zoneId: String): Result<Unit>
    suspend fun reregisterAll(zones: List<GeofenceZone>): Result<Unit>
    suspend fun unregisterAll(): Result<Unit>
}

interface LocationProvider {
    suspend fun currentLocation(): Result<SimpleLocation>
    fun locationUpdates(intervalMillis: Long = 5_000L): Flow<SimpleLocation>
}

interface PermissionChecker {
    fun current(): PermissionState
    fun observe(): Flow<PermissionState>   // re-emits when refresh() is called
    fun refresh()
}

// ---------- ui/navigation ----------
@Serializable sealed interface Route {
    @Serializable data object Onboarding : Route
    @Serializable data object Home : Route
    @Serializable data class ZoneEditor(val zoneId: String? = null) : Route   // S1
    @Serializable data object History : Route
    @Serializable data object Settings : Route                               // S1
    @Serializable data object About : Route
}

// ---------- screen entry points (A stubs, C and D replace) ----------
@Composable fun ZonelyTheme(themeMode: ThemeMode, dynamicColor: Boolean,
                            content: @Composable () -> Unit)                      // C
@Composable fun HomeRoute(onAddZone: () -> Unit, onEditZone: (String) -> Unit,
                          onOpenHistory: () -> Unit, onOpenSettings: () -> Unit)  // C
@Composable fun ZoneEditorRoute(zoneId: String?, onDone: () -> Unit,
                                onBack: () -> Unit)                               // C
@Composable fun OnboardingRoute(onFinished: () -> Unit)                           // D
@Composable fun HistoryRoute(onBack: () -> Unit)                                  // D
@Composable fun SettingsRoute(onBack: () -> Unit, onOpenAbout: () -> Unit)        // D
@Composable fun AboutRoute(onBack: () -> Unit)                                    // D
```

**Use cases (Dev A):** `SaveZoneUseCase`, `DeleteZoneUseCase`, `ToggleZoneUseCase`, `RecordTransitionUseCase`, `SyncRegistrationsUseCase`, `ObserveZonesUseCase`.

**Debug hook (Dev B):** `simulateTransition(zoneId: String, transition: TransitionType)`, debug source set only, routed through the exact same path as a real transition. Dev C surfaces it on Home in debug builds. This is the demo safety net.

---

## 7. Git workflow

```
main                      protected, PR only, never push directly
feat/core-data            Dev A
feat/geofencing           Dev B
feat/design-system-zones  Dev C
feat/permissions-history  Dev D
```

- Conventional Commits: `feat(geo): register geofence via GeofencingClient`, `fix(ui): guard empty zone list`, `test(data): cover event repository`.
- Small, frequent commits. One logical change each.
- `git fetch origin && git rebase origin/main` before opening a PR.
- One reviewer approval from another member. Squash merge.
- `local.properties` is gitignored and never committed. API keys live there only.

### Test gate (run before every push, no exceptions)

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew installDebug
```

All three Gradle tasks green, then install and manually exercise the screen you changed. Only then push. If the gate fails and you cannot fix it within 10 minutes, push to your branch but mark the PR **draft** and state what is broken.

---

## 8. Emulator geofence test protocol

Geofence transitions are batched by the platform and are slow and flaky on emulators. Plan for it.

1. Use a **Google Play** system image, API 33+. Geofencing needs Play services.
2. Extended Controls (`...`) → Location → set a single point, or load a route and press play.
3. Grant location as **Allow all the time** in system settings. Background location is mandatory for geofences to fire when the app is not foregrounded.
4. Set `setNotificationResponsiveness` low (around 5000ms) during development. Comment that production should use a higher value for battery.
5. Expect 30 to 120 seconds before a transition fires. Wait before declaring it broken.
6. Use the debug simulate hook for the demo, and say out loud which one is simulated.

---

## 9. Rules for AI agents

- Work only inside the paths your prompt assigns you. If a change requires a file outside them, stop and report it rather than editing.
- Read `AGENTS.md` and `DESIGN.md` at the start of every session. They are the source of truth over anything you remember from a previous session.
- Never change a contract signature in Section 6. If one is wrong, report it.
- No hardcoded colours, `sp` values, `dp` paddings or corner radii outside `ui/theme/`. A design system is being dropped in later and must be a one-file swap.
- Never swallow an exception silently. Log it and return a typed failure.
- Do not add a dependency yourself. Ask Dev A.
- Do not commit `local.properties`, API keys, or generated build output.
- Run the test gate before every push. Do not report a task complete without it.
- Prefer finishing an S0 item over starting an S1 item, always.

---

## 10. Definition of done

**S0:** fresh install walks the full permission flow without a dead end; the active geofence and its radius are clearly labelled on Home; ENTER and EXIT both produce a visible alert and a timestamped history entry; two zones are distinguishable in the alert; denial paths do not crash; light and dark both render; `docs/permission-flow.md` is committed; unit tests pass; the gate is green.

**S1 and S2:** as defined in Section 2, each behind its own PR, each with the same gate.
