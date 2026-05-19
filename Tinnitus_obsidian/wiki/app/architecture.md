---
title: "App Architecture"
type: app
aliases: ["Architecture", "Android Architecture"]
tags: [app/architecture]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-19
---

# App Architecture (Android)

Android-only. iOS is out of scope. See [[wiki/app/prd]] for product scope.

## Tech stack
- **Language / UI:** Kotlin + Jetpack Compose
- **Pattern:** MVVM + Repository
- **Persistence:** Room v5 (relational), DataStore Preferences (key-value)
- **Audio:** Native `AudioTrack` + custom DSP (`BiquadFilter`) for **NOTCH** (broadband notch) and **AMPLIFY** (narrow bandpass) modes at the matched frequency. Procedural color noise (pink/white/brown via 1-pole lowpass). Synchronized `MediaPlayer` ambient layers (rain, beach).
- **Background audio:** Foreground Service (`TherapyAudioService`, `foregroundServiceType="mediaPlayback"`) ✓

## Folder / package layout

```
com.tinnitustracker/
├── TinnitusTrackerApp.kt       ← Application subclass; owns AudioEngine singleton ✓
│
├── audio/
│   ├── engine/                 ← AudioEngine.kt (NOTCH + AMPLIFY DSP, color noise, ambient) ✓
│   │                             BiquadFilter.kt ✓
│   ├── service/                ← TherapyAudioService ✓, AudioNotification ✓
│   └── routing/                ← AudioFocusController ✓ (BecomingNoisy planned)
│
├── data/
│   ├── database/               ← AppDatabase v5 ✓
│   │   ├── entities/           ← DiaryEntry ✓, TFIAssessment ✓, ListeningSession ✓,
│   │   │                         ListeningSessionSegment ✓, SoundPreset ✓
│   │   └── dao/                ← DiaryDao ✓, TfiAssessmentDao ✓, ListeningSessionDao ✓,
│   │                             ListeningSessionSegmentDao ✓, SoundPresetDao ✓
│   ├── preferences/            ← PreferenceKeys.kt ✓
│   └── repository/             ← AudioRepository ✓, UserSettingsRepository ✓,
│                                 DiaryRepository ✓, TfiRepository ✓,
│                                 ListeningSessionRepository ✓, SoundPresetRepository ✓
│
├── domain/                     ← pure-Kotlin business logic (planned — not yet created)
│
├── ui/
│   ├── home/                   ← HomeScreen ✓, HomeViewModel ✓
│   │                             (adherence ring, sleep timer sheet, TFI prompt card,
│   │                              quick log integration, treatment-week label)
│   ├── sounds/                 ← SoundSettingsScreen ✓, SoundSettingsViewModel ✓
│   │                             (preset switcher, color-noise pills, ambient sliders,
│   │                              processing-mode segmented control, preview button)
│   ├── matcher/                ← FrequencyMatchingScreen ✓, FrequencyMatchingViewModel ✓
│   │                             (subscreen of 소리; logarithmic dial, notch/amplify selector,
│   │                              seamless therapy handoff)
│   ├── records/                ← RecordsScreen ✓, RecordsViewModel ✓
│   │                             DayDetailBottomSheet ✓, QuickLogBottomSheet ✓
│   │                             (2-tab: 개요 및 기록 | TFI 점수; heatmap calendar,
│   │                              weekly summary cards, recent entries list, TFI sparkline,
│   │                              FAB quick-log, day-tap drilldown, delete entries)
│   ├── assessment/             ← TfiQuestionnaireScreen ✓, TfiResultsScreen ✓,
│   │                             TfiQuestionnaireViewModel ✓, TfiScoring ✓, TfiContent ✓
│   │                             (25-item TFI; 8-page subscale pagination; MCID = 13)
│   ├── onboarding/             ← OnboardingScreen ✓, OnboardingViewModel ✓
│   │                             (7-page HorizontalPager; gated on first launch;
│   │                              re-playable from Settings)
│   ├── settings/               ← SettingsScreen ✓
│   ├── components/             ← MiniPlayer ✓ (persistent bar above bottom nav)
│   ├── therapy/                ← TherapyScreen (planned)
│   └── theme/
│       ├── Color.kt ✓          ← full palette incl. Heat0–Heat4 heatmap ramp
│       ├── Theme.kt ✓          ← light/cream app-wide theme; Matcher stays dark
│       ├── Type.kt ✓
│       ├── Tokens.kt ✓         ← Spacing (4 dp grid), Radius, Elevation token objects
│       ├── PressEffect.kt ✓    ← pressableClickable, PolishedPlayButton, PlayButtonVariant
│       └── components/         ← shared component library ✓
│           AppCard, PrimaryButton, SecondaryButton, Pill, TagChip,
│           SegmentedControl, IconBadge, ChevronRow, EmptyStateCard, SectionHeader
│
└── MainActivity.kt             ← 4-tab scaffold (홈 / 소리 / 기록 / 설정) ✓
                                  소리 owns Matcher subscreen; 설정 owns TFI + TFI Results subscreens
```

## State management

UI state flows **Repository → ViewModel → Compose**. Repositories are the only layer that reads from or writes to Room or DataStore. `AudioEngine` does not own user state; it observes Repository state and reflects it.

## Data model — Room entities

**Shipped (Room v5)**

| Entity | Purpose | Notable fields |
|---|---|---|
| `DiaryEntry` | Daily symptom + stress + tag journal | id, date (epoch ms), severity (0–10), stressLevel (0–10), note (tags packed comma-separated) |
| `TFIAssessment` | Tinnitus Functional Index — bi-weekly by default | id, takenAtEpochMs, items (JSON), totalScore, subscaleScores (JSON); MCID = 13 |
| `ListeningSession` | Auto-recorded playback session; min floor = **3 min (180 000 ms)** | id, startedAtEpochMs, endedAtEpochMs, durationMs, presetLabel? |
| `ListeningSessionSegment` | Per-sound slice of a session — split on every sound change | id, sessionId (FK), soundId/presetLabel, startedAtEpochMs, durationMs |
| `SoundPreset` | Saved sound mix configuration | id, name, processingMode (`notch`/`amplify`/`off`), colorNoise (`pink`/`white`/`brown`/`none`), colorNoiseVolume, ambientMix, createdAt, lastUsedAt |

> **`ListeningSession` min-floor** — 3 min (180 000 ms), raised from 5 s on 2026-05-14. Compromise between noise filtering and not silently dropping legitimate short TRT exposures. See [[wiki/sources/gemini-records-architecture-2026-05]].

> **Assessment instrument** — handwritten plan (`raw/Plan(수기).md`) references "THI 설문지". Codebase uses **TFI** (Tinnitus Functional Index) — newer, treatment-responsive, designed to detect change over time. User-facing copy uses "이명 설문지" to avoid confusion.

**Advanced (Tier 1+)** — gated behind later tiers in [[wiki/app/plan]]

| Entity | Purpose |
|---|---|
| `MixingPointCalibration` | Per-ear calibrated dB level for [[wiki/clinical/sound-therapy]] |
| `SpikeEvent` | Logged tinnitus spikes |
| `UserProgress` | TRT phase tracking |

Schema versioning via Room migrations. No destructive migrations on production data.

## DataStore preferences

**Implemented (in `PreferenceKeys.kt`):**

| Key | Type | Purpose |
|---|---|---|
| `matched_frequency_hz` | Float | Confirmed tinnitus pitch (default 1000 Hz) |
| `has_tonal_tinnitus` | Boolean? | `true` = confirmed; `false` = skipped; `null` = undecided |
| `processing_mode` | String | `"notch"` / `"amplify"` (default `"notch"`) |
| `onboarding_complete` | Boolean | Gates onboarding on first launch (default `false`) |
| `active_preset_id` | Long | Currently-selected `SoundPreset` |
| `daily_listening_goal_min` | Int | Calendar day-complete threshold (default 120 min) |
| `treatment_start_date` | Long | Epoch ms of first therapy session start |
| `tfi_cadence_weeks` | Int | TFI re-assessment cadence: `1` or `2` (default `2`) |
| `last_tfi_date` | Long | Epoch ms; drives TFI re-assessment prompt (default 0) |

**Planned:**

| Key | Purpose |
|---|---|
| `sleep_timer_default_min` | Last-used sleep timer duration |
| `tinnitus_explainer_seen` | Tutorial-mode explainer dismissed |
| `current_trt_phase` | _(Tier 1)_ Cached TRT phase |
| `last_red_flag_screen_date` | _(Tier 1)_ 30-day red-flag screener cadence |

DataStore holds **lightweight, frequently-read** values. Anything historical or relational lives in Room.

## Audio Foreground Service ✓

Pattern: **Application-singleton engine + started-only FGS** (not bound). `AudioEngine` has no Context-dependent state; ViewModel StateFlow exposures need a stable non-null reference.

- `TinnitusTrackerApp : Application` owns `AudioEngine` as `by lazy` singleton.
- `TherapyAudioService` — started only, no `onBind`. Actions: `ACTION_START` (startForeground), `ACTION_STOP` (engine.stop + stopForeground + stopSelf), `ACTION_PAUSE`, `ACTION_RESUME`. `foregroundServiceType="mediaPlayback"`, `exported="false"`.
- `AudioRepository` — single UI access point. Forwards engine StateFlows; `start()` → startForegroundService then engine.start; `stop()` → engine.stop then stopService; `pause()` / `resume()` stop/restart engine audio while keeping FGS alive and session open.
- `AudioNotification` — low-importance channel `therapy_audio`, NOTIF_ID 1001; action button swaps between "일시정지" and "재생" based on `isPlaying`; content tap → `MainActivity` (`launchMode="singleTop"`).
- Architecture rule: `ui → AudioRepository → AudioEngine`. ViewModels never reference `AudioEngine` directly.

## Audio focus & interruption handling ✓

`AudioFocusController` owns a single `AudioFocusRequest` (USAGE_MEDIA / CONTENT_TYPE_MUSIC; `setWillPauseWhenDucked(true)`). Its listener mirrors LOSS/GAIN into its own `hasFocus` flag so re-requesting after permanent loss actually re-asks the system.

`AudioRepository` focus policy:
- `start()` — requests focus before promoting service / starting engine.
- `stop()` (user) — abandons focus, tears down service.
- `LOSS` → full teardown; clears `wasPlayingBeforeInterruption` latch.
- `LOSS_TRANSIENT` / `LOSS_TRANSIENT_CAN_DUCK` → engine off, **service stays in foreground**, latch set.
- `GAIN` after transient → engine restarts; latch cleared.

## Design system

**Tokens** (`ui/theme/Tokens.kt`):
- `Spacing` — 4 dp grid: xs=4, sm=8, md=12, lg=16, xl=20, xxl=24, section=32. Forbidden literals (must not appear in UI code): 5, 6, 9, 10, 14, 18, 22, 26, 28 dp.
- `Radius` — card=16 dp, chip=8 dp, button=12 dp, pill=100 dp.
- `Elevation` — card=2 dp, raised=4 dp, hero=8 dp.

**Component library** (`ui/theme/components/`): `AppCard` (5 variants), `PrimaryButton`, `SecondaryButton`, `Pill` (4 variants × 2 sizes), `TagChip`, `SegmentedControl`, `IconBadge`, `ChevronRow`, `EmptyStateCard`, `SectionHeader`. Adding a new variant = extend the enum, not build a parallel component.

**Press effects** (`ui/theme/PressEffect.kt`): `pressableClickable` (drop-in for `.clickable`; 0.96 press-scale, spring dampingRatio=0.62/stiffness=820). `PolishedPlayButton` with `PlayButtonVariant` enum (`Coral`, `Orange`, `Teal`). Every interactive surface except `TabRow` and `Slider` uses `pressableClickable`.

**Theme:** Light/cream app-wide (status bar light icons, white `NavigationBar`, Teal active). `FrequencyMatchingScreen` stays dark (bespoke lab aesthetic — exempt from token rules for its metallic-knob geometry).

## Module dependency rules

```
ui  →  domain  →  data
ui  →  audio  (only via AudioRepository)
audio/service  →  audio/engine
data/repository  →  data/database  +  data/preferences
```

- `ui` never touches `data` directly — always through a Repository.
- `audio/engine` has no Compose / Activity dependencies (testable in isolation).
- `domain` is pure Kotlin (no Android imports) — not yet created.

## See also
- [[wiki/app/prd]] — product scope
- [[wiki/app/plan]] — implementation sequence
- [[wiki/app/android-snapshot]] — historical code snapshot (2026-05-11; may be stale)
