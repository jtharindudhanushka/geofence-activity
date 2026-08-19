# DESIGN.md

Design system and UX specification for **Zonely**.
Read alongside `AGENTS.md`. Dev C owns everything in `ui/theme/` and `ui/components/`; everyone else consumes it.

---

## 1. Principles

1. **State is always visible.** The user should never wonder whether the app is watching. Location status, zone status and permission status are always on screen.
2. **Explain before you ask.** No system permission dialog appears without a plain-language reason immediately before it.
3. **Never a dead end.** Every denial, error and empty state offers a next action.
4. **Expressive, not decorative.** Motion and shape communicate state change (entered, exited, active, inactive). If an animation does not carry meaning, remove it.
5. **One-file swap.** Every visual value lives in a token file. A new design system replaces `Color.kt`, `Type.kt`, `Shape.kt` and `Spacing.kt` and nothing else changes.

---

## 2. Token architecture

```
ui/theme/
├── Color.kt      the ONLY file in the repo containing hex literals
├── Type.kt       the ONLY file containing sp values
├── Shape.kt      the ONLY file containing corner radii
├── Spacing.kt    the ONLY file containing raw dp spacing, exposed via CompositionLocal
├── Motion.kt     motion scheme and reusable transition specs
└── ZonelyTheme.kt  MaterialExpressiveTheme wrapper
```

**Enforcement rule:** a hardcoded `Color(0xFF...)`, `14.sp`, `RoundedCornerShape(12.dp)` or `padding(16.dp)` anywhere outside `ui/theme/` is treated as a build failure in review. Use `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*` and `LocalSpacing.current.*`.

---

## 3. Colour

Full M3 tonal roles, light and dark, both hand-checked for contrast (4.5:1 body text, 3:1 large text and UI components).

Required roles in `Color.kt`:

```
primary, onPrimary, primaryContainer, onPrimaryContainer
secondary, onSecondary, secondaryContainer, onSecondaryContainer
tertiary, onTertiary, tertiaryContainer, onTertiaryContainer
error, onError, errorContainer, onErrorContainer
background, onBackground
surface, onSurface, surfaceVariant, onSurfaceVariant
surfaceContainerLowest, surfaceContainerLow, surfaceContainer,
surfaceContainerHigh, surfaceContainerHighest
outline, outlineVariant
inverseSurface, inverseOnSurface, inversePrimary
scrim
```

### Semantic mapping

| Meaning | Role |
|---|---|
| Inside a zone | `primaryContainer` / `onPrimaryContainer` |
| Outside a zone | `surfaceContainerHigh` / `onSurfaceVariant` |
| Zone inactive | `surfaceVariant`, reduced opacity content |
| Permission missing, blocking | `errorContainer` / `onErrorContainer` |
| Permission partial, non-blocking | `tertiaryContainer` / `onTertiaryContainer` |
| ENTER event | `primary` |
| EXIT event | `secondary` |
| DWELL event (S1) | `tertiary` |

### Zone accent palette

Six fixed accents users pick from per zone, stored as `colorArgb` on `GeofenceZone`. They must be distinguishable in both light and dark, and never used as the only carrier of meaning (always pair with the zone name or an icon).

### Dynamic colour

On API 31+, when `AppSettings.dynamicColorEnabled` is true, use `dynamicLightColorScheme` / `dynamicDarkColorScheme`. Fall back to the static scheme otherwise. Zone accents are **not** dynamic; they stay fixed so a zone keeps its identity.

---

## 4. Typography

Full M3 Expressive scale in `Type.kt`: display / headline / title / body / label, each in small, medium and large, plus the emphasized variants where the library exposes them.

| Usage | Style |
|---|---|
| Hero status ("Inside Home") | `displaySmall` or `headlineLarge` emphasized |
| Screen title in large top app bar | `headlineMedium` |
| Zone name in list | `titleMedium` |
| Supporting metadata (radius, coordinates) | `bodyMedium` on `onSurfaceVariant` |
| Timestamps, chips, captions | `labelMedium` |
| Buttons | `labelLarge` |

Never scale text with a hardcoded `fontSize`. Test at 200% system font scale; nothing may clip or truncate a critical value.

---

## 5. Shape

M3 Expressive uses noticeably larger radii than earlier M3. Lean into it.

| Element | Shape token |
|---|---|
| Chips, small buttons | `extraSmall` / `small` |
| List items, cards | `medium` / `largeIncreased` |
| Hero status card | `extraLarge` |
| Bottom sheets, dialogs | `extraLarge` |
| FAB, toggles | `full` |

Use shape morphing from `MaterialShapes` on the zone active toggle and on transition-type selection, so the shape change itself signals state.

---

## 6. Spacing, elevation, motion

**Spacing scale:** `xs 4`, `sm 8`, `md 16`, `lg 24`, `xl 32`, `xxl 48` (dp), via `LocalSpacing`.
Screen horizontal padding is `md`. Vertical rhythm between sections is `lg`. Gap inside a card is `sm`.

**Elevation:** prefer tonal elevation (surface container roles) over shadows. Shadow only on the FAB and on scrolled app bars.

**Motion:** `MotionScheme.expressive()`. Transition alerts animate in with a spring, not a linear tween. Zone status changes cross-fade the container colour over roughly 300ms. Respect the system reduce-motion setting.

---

## 7. Material 3 Expressive component map

Use these where they genuinely fit. If a symbol does not resolve in the pinned alpha, fall back to the closest stable M3 component and leave `// TODO(M3E):`.

| Component | Where |
|---|---|
| `ButtonGroup` | Transition-type selection (ENTER / EXIT / DWELL), history filters |
| `SplitButton` | Save and activate on the zone editor (S1) |
| `LoadingIndicator` / `ContainedLoadingIndicator` | Any wait under 5 seconds, instead of indeterminate `CircularProgressIndicator` |
| `FloatingActionButtonMenu` | Add-zone FAB on Home (S1) |
| `HorizontalFloatingToolbar` | Zone editor actions (S1) |
| Expressive `ListItem` variants | Zone list, history rows |
| `LinearWavyProgressIndicator` | Determinate progress, e.g. onboarding step progress |
| `MaterialShapes` morphing | Active toggle, filter selection |
| Large flexible top app bar | Every screen title |

---

## 8. Screen specs

### 8.1 Home (Dev C, S0)

Answers SRS R5: which geofence is active, where it is, how big it is.

```
[ Large top app bar: "Zonely"                      ⋮ ]
[ HERO STATUS CARD                                    ]
[   "Inside Home Zone" / "Outside Home Zone"          ]
[   distance to edge, e.g. "68 m from the edge"       ]
[   current fix accuracy, e.g. "accuracy ±12 m"       ]
[ PermissionBanner (only when something is missing)   ]
[ ACTIVE ZONES                                        ]
[   ▢ Home Zone   200 m   ENTER · EXIT      [toggle]  ]
[   ▢ Campus      300 m   ENTER · EXIT      [toggle]  ]
[ RECENT ACTIVITY  (last 3, "See all" → History)      ]
[   ↳ Entered Home Zone      2 min ago                ]
[ (debug builds only) Simulate ENTER / EXIT           ]
                                            [ + FAB ]  (S1)
```

- Hero card colour flips between the inside and outside semantic roles, cross-faded.
- Distance computed with `Location.distanceTo` or Haversine. Do not invent a formula.
- Overflow menu: History, Settings (S1), About.

**States:** loading (first frame), permission missing (banner plus disabled toggles), location services off (banner with a settings action), no zones (S1 empty state), error.

### 8.2 Onboarding and permissions (Dev D, S0)

One ask per screen, reason shown before the system dialog. Progress indicator across the top.

| Step | Content | API branch |
|---|---|---|
| 1 Welcome | What Zonely does. No ask. | all |
| 2 Foreground location | Request FINE + COARSE together. Reason: to know where you are relative to your zones. | all |
| 3 Background location | Separate request **after** foreground is granted. On API 30+ the system shows no dialog, so deep-link to app settings and tell the user to pick "Allow all the time". Reason: geofences fire when the app is closed. | API 29+ |
| 4 Notifications | Request `POST_NOTIFICATIONS`. | API 33+ only, otherwise skipped |
| 5 Location services | If disabled, prompt and deep-link to location settings. | all |

Three outcomes per step:
- **Granted** → advance.
- **Denied once** → `shouldShowRequestPermissionRationale` is true → show rationale, offer retry.
- **Permanently denied** → rationale returns false after a denial → hide retry, show a settings deep link.

Always offer "Continue with limited functionality". Never trap the user.

Refresh state on `ON_RESUME` via a `LifecycleEventObserver` calling `PermissionChecker.refresh()`, so returning from system settings updates instantly.

### 8.3 History (Dev D, S0 for R6)

Reverse-chronological list, day-grouped with sticky headers.

Row: zone accent dot, zone name (`titleMedium`), transition icon and label, relative time with the absolute timestamp beneath, coordinates and accuracy when present.

S1 adds zone and type filters as a `ButtonGroup`, and clear-all behind a confirmation dialog.
Empty state: "No crossings yet. Move across a zone boundary and it will show up here."

### 8.4 Zone editor (Dev C, S1)

Map with a centre marker and a live `Circle` following the radius slider. Tap to move the centre, drag to fine-tune. "Use my current location" button. Name field with inline validation, radius slider 50m to 10,000m, transition-type `ButtonGroup`, accent picker, active switch.

**Required fallback:** when `MAPS_API_KEY` is absent, hide the map and show manual latitude and longitude fields plus a short note. The screen stays fully usable. Test this path by temporarily blanking the key.

Validation errors appear inline on the offending field, never as a generic toast. Back with unsaved changes prompts for confirmation.

### 8.5 Settings and About (Dev D, S1 / S2)

Settings: notifications, vibration, theme mode, dynamic colour, distance unit, redo permission setup, clear history, re-register all zones.
About: version, stack, all five team member names and roles.

---

## 9. Notifications

| Transition | Title | Body |
|---|---|---|
| ENTER | Entered {zone} | Arrived at {zone}, {time} |
| EXIT | Left {zone} | Departed {zone}, {time} |
| DWELL (S1) | Still at {zone} | You have been at {zone} for {duration} |

Channel importance HIGH. Distinct icon per transition. Distinct notification id per zone so simultaneous alerts do not overwrite each other. Content intent deep-links to History via `TaskStackBuilder`. Respect `notificationsEnabled` and `vibrationEnabled`. If `POST_NOTIFICATIONS` is not granted on API 33+, no-op quietly instead of throwing.

---

## 10. Copy guidelines

- Second person, present tense, no jargon in user-facing text. "Zonely needs to see your location even when the app is closed" beats "Background location permission required".
- Errors say what happened and what to do next. "Location services are off. Turn them on to track your zones." with the action attached.
- No exclamation marks. No "Oops".
- Every string goes in `strings.xml`, under your own dev comment block at the bottom of the file.

---

## 11. Accessibility checklist (blocking, not optional)

- Content description on every icon and icon button. Decorative images get `null`.
- Minimum 48dp touch targets everywhere.
- `mergeDescendants` on cards and list rows so TalkBack reads one coherent item.
- Colour is never the only signal. Inside/outside also carries text and an icon.
- Logical focus order; the onboarding flow must be completable end to end with TalkBack. A permission flow a screen reader user cannot complete is a failed permission flow.
- Verify at 200% font scale and in a compact-width window with no clipping.
- Respect reduce-motion.

---

## 12. Preview requirements

Every component and every screen gets `@Preview` in light and dark. Screens additionally get previews for each meaningful state: loading, empty, error, permission missing, permission partial. Previews are how the team reviews visual work without pulling the branch, so they are part of the definition of done.
