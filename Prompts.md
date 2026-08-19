# Group 5 - Agent Prompts v2 (S0 scoped)

These replace the v1 prompts. They are shorter because `AGENTS.md` and `DESIGN.md` now live in the repo and carry the shared detail. Every prompt starts by making the agent read those two files.

**Send each person only their own section. Dev A goes first.**

---

## Step 0 - land the docs before anything else

Dev A (or whoever holds the repo) runs this first, so the two files exist before any agent starts:

```bash
git clone https://github.com/jtharindudhanushka/geofence-activity.git
cd geofence-activity
# copy AGENTS.md and DESIGN.md into the repo root
git add AGENTS.md DESIGN.md
git commit -m "docs: add AGENTS.md and DESIGN.md as the shared build contract"
git push origin main
```

Then tell the group: pull `main` before starting.

---
---

# PROMPT A - Core, Data and Skeleton (Dev A, repo lead)

You are Dev A on a 4-developer parallel Android build, working through the Gemini agent in Android Studio.

Repo: `https://github.com/jtharindudhanushka/geofence-activity.git`
Branch: `feat/core-data`

**Before you write any code, open and read `AGENTS.md` and `DESIGN.md` in the repo root. They define the scope tiers, the locked stack, the package ownership map, the shared contracts, the git workflow and the test gate. They override anything you assume. Re-read them at the start of every session.**

You own: the project skeleton, Gradle and the version catalog, the manifest, Hilt wiring, all domain models, all shared interfaces, the data layer, the use cases, and the navigation host. See the ownership table in `AGENTS.md` Section 4.

**Scope: build S0 only. Do not start S1 until S0 is merged and demoable.** S0 means an in-memory data layer, not Room. Room and DataStore are S1, behind the same interfaces.

## S0 tasks, in order

1. **Skeleton and contracts, push this first, three people are blocked on it.**
   - Android project with Compose, Hilt, Kotlin DSL Gradle, `gradle/libs.versions.toml`, KSP. `.gitignore` covering `local.properties`, `/build`, `.idea/`, `*.apk`, `.gradle`.
   - `AndroidManifest.xml` with `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`. Leave a clearly commented empty region at the bottom of `<application>` for Dev B's receivers.
   - `ZonelyApp` (`@HiltAndroidApp`) and `MainActivity` (`@AndroidEntryPoint`, `enableEdgeToEdge()`, content is `ZonelyTheme { ZonelyNavHost() }`).
   - Every model, repository interface and geo interface **exactly as written in `AGENTS.md` Section 6**. Do not rename anything.
   - `ui/navigation/Route.kt` and `ZonelyNavHost.kt`. Start destination decided by `AppSettings.onboardingCompleted`.
   - One-line placeholder files for the six screen entry points and `ZonelyTheme`, with the exact signatures from Section 6 and a header comment `// OWNER: Dev C - replace this entire file`. After this commit you never touch them again.
   - PR, merge, tell the team `main` is ready.
2. **In-memory data layer.** `InMemoryGeofenceZoneRepository` seeded with two hardcoded zones (this satisfies SRS R2 and R7). `InMemoryGeofenceEventRepository` as a bounded ring buffer exposed as a `StateFlow`. `InMemorySettingsRepository`. All behind the Section 6 interfaces so S1 swaps in Room with zero changes elsewhere. Suspend work on an injected `DispatchersProvider`.
3. **Hilt modules** in `core/di/`: dispatchers, clock, repository `@Binds`. **Do not bind `GeofenceRegistrar`, `LocationProvider` or `PermissionChecker`.** Dev B owns those in `geo/di/GeoModule.kt`.
4. **Use cases:** `ObserveZonesUseCase`, `ToggleZoneUseCase`, `RecordTransitionUseCase` (resolves the zone name and writes the event), `SyncRegistrationsUseCase` (reads active zones, calls `reregisterAll`). `SaveZoneUseCase` and `DeleteZoneUseCase` can be written now with validation but are only exercised in S1.
5. **Utilities:** `DistanceFormatter` (metres to readable, switching to km above 1000m, respecting `DistanceUnit`) and `TimeFormatter` (relative plus absolute). Locale-aware.
6. **Tests:** every use case with MockK fakes for `GeofenceRegistrar`, the in-memory repositories with Turbine, `SaveZoneUseCase` validation edge cases, `DistanceFormatter` boundaries.
7. **README.md:** overview, architecture, module map, how to run the test gate, and a pointer to `AGENTS.md` and `docs/permission-flow.md`.

## S1 backlog (do not start until S0 is merged)

Room database, DAOs, entities, converters, mappers, migration from the in-memory seed, DataStore settings, secrets Gradle plugin for `MAPS_API_KEY`, PR template.

## Rules

Ownership, git workflow, commit convention and the test gate are all in `AGENTS.md`, Sections 4, 7 and 9. Follow them exactly. Strings go under `<!-- ===== DEV A ===== -->` at the bottom of `strings.xml`. If a task needs a file you do not own, stop and report it.

**Done when:** skeleton merged early, contracts present and unchanged, in-memory layer working, tests passing, the app builds, installs and navigates all six placeholder screens without crashing.

---
---

# PROMPT B - Geofencing, Location and Notifications (Dev B)

You are Dev B on a 4-developer parallel Android build, working through the Gemini agent in Android Studio.

Repo: `https://github.com/jtharindudhanushka/geofence-activity.git`
Branch: `feat/geofencing`

**Before you write any code, open and read `AGENTS.md` and `DESIGN.md` in the repo root. They define the scope tiers, the locked stack, the ownership map, the shared contracts, the git workflow, the test gate and the emulator geofence protocol. They override anything you assume. Re-read them at the start of every session.**

You own everything under `geo/` and `notification/`. You implement the three interfaces in `AGENTS.md` Section 6 that Dev A declares: `GeofenceRegistrar`, `LocationProvider`, `PermissionChecker`. You own the permission **checking logic**, not the permission UI (that is Dev D).

If A's `main` has not landed yet, stub the Section 6 contracts locally so you can compile, then delete the stubs and rebase when A pushes. **Never push a local copy of A's files.**

**Scope: build S0 only.** Boot re-registration, DWELL and the foreground service are S1.

## S0 tasks

1. **`GeofenceRegistrarImpl`** over `GeofencingClient`:
   - Map `GeofenceZone` to `Geofence`: `setRequestId(zone.id)`, `setCircularRegion(...)`, `setExpirationDuration(NEVER_EXPIRE)`, transition mask built from `transitionTypes`, and `setNotificationResponsiveness` around 5000ms for development with a comment that production should be higher.
   - `GeofencingRequest` with `INITIAL_TRIGGER_ENTER` so the app tells the user immediately if they are already inside.
   - `PendingIntent` with `FLAG_UPDATE_CURRENT or FLAG_MUTABLE`, held as a **lazy singleton**. Creating a new one per call silently breaks removal.
   - Suspend wrapper over the `Task` API (`kotlinx-coroutines-play-services` `await()` or `suspendCancellableCoroutine`), wrapped in `runCatching` to return `Result`.
   - **Check permission before every call.** `addGeofences` throws `SecurityException` without it. Return a typed failure, never crash.
   - Extract the transition-mask conversion into a pure function so it is unit testable.
2. **`GeofenceErrorMapper`:** `GeofenceStatusCodes` to human-readable strings in `strings.xml`. Cover `GEOFENCE_NOT_AVAILABLE` (usually location services off or Wi-Fi scanning disabled), `TOO_MANY_GEOFENCES`, `TOO_MANY_PENDING_INTENTS`, `INSUFFICIENT_LOCATION_PERMISSION`.
3. **`GeofenceBroadcastReceiver`** (`@AndroidEntryPoint`):
   - `GeofencingEvent.fromIntent(intent)`, null-check it, check `hasError()` first and log the mapped error.
   - Handle **all** of `triggeringGeofences`, not just the first. Read `triggeringLocation` for lat/lng/accuracy.
   - `goAsync()` with a coroutine scope, and always `pendingResult.finish()` in a `finally`.
   - For each geofence: call `RecordTransitionUseCase`, then the notifier. This satisfies SRS R4 and R7.
   - Extract the parsing into a plain function that takes a parsed event and returns domain events, so it is unit testable. Do not try to unit test the receiver class.
   - Register it in Dev A's commented manifest region, `android:exported="false"`. Nowhere else.
4. **Notifications:** `NotificationChannels` (IMPORTANCE_HIGH, created on app start) and `TransitionNotifier`. Copy, icons, ids and deep-link behaviour are specified in `DESIGN.md` Section 9. Distinct notification id per zone. Quiet no-op if `POST_NOTIFICATIONS` is not granted on API 33+.
5. **`FusedLocationProviderImpl`:** `currentLocation()` via `getCurrentLocation(PRIORITY_HIGH_ACCURACY, CancellationTokenSource())` with a timeout, falling back to `lastLocation` then a typed failure. `locationUpdates()` as a `callbackFlow` around `requestLocationUpdates`, removing updates in `awaitClose`. This powers the Home hero card, so it must not leak.
6. **`PermissionCheckerImpl`:** `ContextCompat.checkSelfPermission` for each, background location only on API 29+, `POST_NOTIFICATIONS` only on API 33+, plus `LocationManager.isLocationEnabled`. `observe()` is a `MutableStateFlow` that `refresh()` updates. Dev D calls `refresh()` from a lifecycle observer.
7. **`geo/di/GeoModule.kt`:** Hilt bindings for the three interfaces plus providers for `GeofencingClient` and `FusedLocationProviderClient`. Only this file. Do not add bindings to Dev A's modules.
8. **Debug simulate hook (do this, it protects the demo):** a `debug`-source-set-only `simulateTransition(zoneId: String, transition: TransitionType)` routed through the exact same use case and notifier path as a real transition. Give Dev C the signature as soon as it exists.
9. **Tests:** error mapper for every status code, transition-mask conversion, `GeofenceRegistrarImpl` with a mocked `GeofencingClient` (success, `SecurityException`, failed `Task`), and the extracted receiver parsing function.
10. **`docs/geofence-manual-test.md`:** the emulator steps you ran and the results. This is demo evidence.

## S1 backlog

`BootCompletedReceiver` re-registering active zones on `ACTION_BOOT_COMPLETED` and `ACTION_MY_PACKAGE_REPLACED`, DWELL support with `setLoiteringDelay`, optional foreground service.

## Rules

`AGENTS.md` Sections 4, 7, 8 and 9. Strings under `<!-- ===== DEV B ===== -->`. Never swallow an exception. If a task needs a file outside `geo/` or `notification/`, stop and report it.

**Done when:** zones register, ENTER and EXIT both produce a notification and a persisted event with the correct zone name, transitions survive an app kill, every error path returns a readable message instead of a crash, tests pass, manual test log committed, and you can explain out loud why geofencing needs background location.

---
---

# PROMPT C - Design System and Home (Dev C)

You are Dev C on a 4-developer parallel Android build, working through the Gemini agent in Android Studio.

Repo: `https://github.com/jtharindudhanushka/geofence-activity.git`
Branch: `feat/design-system-zones`

**Before you write any code, open and read `AGENTS.md` and `DESIGN.md` in the repo root. `DESIGN.md` is your primary specification: it defines the token architecture, the colour roles and semantic mapping, the type and shape scales, the spacing tokens, the Material 3 Expressive component map, the Home screen layout and the accessibility checklist. Follow it precisely. Re-read both at the start of every session.**

You own `ui/theme/`, `ui/components/`, `ui/home/` and `ui/zoneeditor/`.

If A's `main` has not landed yet, stub the `AGENTS.md` Section 6 contracts locally so you can compile, then delete the stubs and rebase. **Never push a local copy of A's files.**

**Scope: build S0 only.** The zone editor and the map are S1. Do not open `maps-compose` until S0 is merged.

## S0 tasks, in order

1. **Theme layer first, push it immediately, Dev D cannot style anything until it exists.** `Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Motion.kt`, `ZonelyTheme.kt`, built exactly to `DESIGN.md` Sections 2 through 6. Exact signature: `@Composable fun ZonelyTheme(themeMode: ThemeMode, dynamicColor: Boolean, content: @Composable () -> Unit)`. This replaces Dev A's placeholder entirely. Then tell Dev D it is on `main`.
2. **Shared components** in `ui/components/`: `ZonelyTopAppBar` (large flexible variant with scroll behaviour), `ZoneCard`, `StatusPill`, `EmptyState`, `ErrorBanner`, `PermissionBanner` (slot-based, Dev D fills the content), `LoadingState`, `ConfirmDialog`. All stateless: state in, lambdas out, no ViewModel references. `@Preview` in light and dark for each.
3. **Home screen**, replacing Dev A's placeholder. Exact signature: `@Composable fun HomeRoute(onAddZone: () -> Unit, onEditZone: (String) -> Unit, onOpenHistory: () -> Unit, onOpenSettings: () -> Unit)`. Layout and states are specified in `DESIGN.md` Section 8.1. It must satisfy SRS R5: the active geofence's location and radius clearly labelled on screen.
   - `HomeViewModel` exposing one `HomeUiState` via `StateFlow`, combining `ObserveZonesUseCase`, `PermissionChecker.observe()`, `LocationProvider.locationUpdates()` and `SettingsRepository.settings`. Collect with `collectAsStateWithLifecycle()`.
   - Hero status card: inside or outside, live distance to the edge, current fix accuracy. Compute distance with `Location.distanceTo` or Haversine, do not invent a formula.
   - Zone list with expressive `ListItem`s: name, radius, transition-type chips, accent colour, active toggle.
   - Recent activity: last three events with a "See all" action to History.
   - **Debug builds only:** simulate ENTER and EXIT per zone, wired to Dev B's `simulateTransition(zoneId, transition)`. Ask B for the signature. This is the demo safety net, do not skip it.
4. **Quality:** the accessibility checklist in `DESIGN.md` Section 11 is blocking. Previews for every state per Section 12.
5. **Compose UI tests:** home renders the empty state with no zones, renders a list with zones, the active toggle calls the right lambda, the hero card shows the inside state when distance is within radius. Fake ViewModels or fake use cases, never real infrastructure.

## S1 backlog

Zone editor with `GoogleMap`, live `Circle` radius overlay, "use my current location", validation, the no-API-key manual lat/lng fallback (see `DESIGN.md` 8.4), `FloatingActionButtonMenu`, swipe-to-delete with undo.

## Rules

`AGENTS.md` Sections 4, 7 and 9. **Zero hardcoded colours, `sp`, `dp` or corner radii outside `ui/theme/`.** This is what makes the incoming design system a one-file swap; treat a violation as a build failure. Strings under `<!-- ===== DEV C ===== -->`. If a task needs a file you do not own, stop and report it.

**Done when:** theme merged early, Home fully functional against real use cases with loading, error and permission-missing states handled, every component previewed in light and dark, UI tests pass, and the app visibly reads as Material 3 Expressive rather than default M3.

---
---

# PROMPT D - Permission UX and History (Dev D)

You are Dev D on a 4-developer parallel Android build, working through the Gemini agent in Android Studio.

Repo: `https://github.com/jtharindudhanushka/geofence-activity.git`
Branch: `feat/permissions-history`

**Before you write any code, open and read `AGENTS.md` and `DESIGN.md` in the repo root. `DESIGN.md` Section 8.2 is the full permission flow specification with the API-level branches, and Section 8.3 is the history spec. Follow them precisely. Re-read both at the start of every session.**

You own `ui/onboarding/`, `ui/history/`, `ui/settings/`, `ui/about/`.

**The permission flow is the single most heavily graded part of this activity, and every team member has to be able to explain it. It is your highest priority.**

If A's `main` has not landed yet, stub the `AGENTS.md` Section 6 contracts locally so you can compile, then delete the stubs and rebase. **Never push a local copy of another dev's file.** You also need Dev C's `ZonelyTheme` before you can style anything; build against the placeholder until C pushes.

**Scope: build S0 only.** Settings and About are S1.

## S0 tasks, in order

1. **Onboarding and permission flow**, replacing Dev A's placeholder. Exact signature: `@Composable fun OnboardingRoute(onFinished: () -> Unit)`. Build every step and every API branch exactly as tabled in `DESIGN.md` Section 8.2. The points that matter most:
   - Background location is a **separate request after** foreground is granted. Bundling them makes Android silently deny background.
   - On API 30+ the system shows no background dialog at all, so deep-link to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` and tell the user to pick "Allow all the time".
   - `POST_NOTIFICATIONS` only on API 33+, skip the step entirely below that.
   - Three outcomes per step: granted, denied once (`shouldShowRequestPermissionRationale` true, show rationale and retry), permanently denied (rationale false after a denial, hide retry and show a settings link).
   - Always offer "Continue with limited functionality". Never a dead end.
   - Refresh on resume: `LifecycleEventObserver` on `ON_RESUME` calling `PermissionChecker.refresh()`, so returning from system settings updates the UI immediately. This detail is what separates a real implementation from a tutorial one.
   - Persist `AppSettings.onboardingCompleted` so it does not run again.
   - Extract the step state machine into a **pure function** (permission state plus API level in, next step out) so it is unit testable.
2. **`PermissionBanner` content** for Dev C's Home slot: a compact state-aware banner (missing foreground, missing background, notifications off, location services off) with a one-tap fix for each.
3. **`docs/permission-flow.md`:** step by step, with the API-level branches, why background location is a separate ask, why geofencing needs it, and what happens in each denial case. **This is the document the whole team reads before the demo.** Write it for a teammate who did not build it.
4. **History screen**, replacing Dev A's placeholder. Exact signature: `@Composable fun HistoryRoute(onBack: () -> Unit)`. Layout in `DESIGN.md` Section 8.3. Satisfies SRS R6: enter and exit events with timestamps, day-grouped, zone name and accent per row, relative time with the absolute timestamp beneath. Empty state included.
5. **Notification deep link:** Dev B's notification opens `MainActivity` with an extra requesting History. Read the intent and navigate, handling the case where the app was already running.
6. **Quality:** the accessibility checklist in `DESIGN.md` Section 11 is blocking. TalkBack must be able to complete the onboarding flow end to end. Previews for every permission state per Section 12.
7. **Tests:** unit test the step state machine across every combination of granted flags and every API branch with a fake `PermissionChecker`; unit test history day-grouping with Turbine; Compose UI tests for onboarding advancing on grant, showing the settings link when permanently denied, and history rendering the empty state.

## S1 backlog

Settings screen (notifications, vibration, theme mode, dynamic colour, distance unit, redo permission setup, clear history, re-register all zones), About screen, history filters as a `ButtonGroup`, clear-all with confirmation, CSV export.

## Rules

`AGENTS.md` Sections 4, 7 and 9. **Zero hardcoded colours, `sp` or `dp`.** Consume Dev C's theme and components only; if you need a new shared component, ask C to add it. Strings under `<!-- ===== DEV D ===== -->`. Test on a **fresh install** every time (`adb uninstall com.group5.zonely` first) so you are exercising the real first-run path. Deliberately test denial: deny once, deny twice, revoke in system settings while running, turn location services off mid-session.

**Done when:** a fresh install walks cleanly through every permission stage with no dead ends and updates instantly on return from system settings, History shows real transitions with timestamps, `docs/permission-flow.md` is committed and presentable by someone who did not write it, and tests pass.
