---
title: "App Architecture"
type: app
aliases: ["Architecture", "Android Architecture"]
tags: [app/architecture]
evidence: n/a
status: draft
sources: []
last_reviewed: 2026-05-11
---

# App Architecture (Android)

Android-only. iOS is out of scope at this time. See [[wiki/app/prd]] for product scope.

## Tech stack
- **Language / UI:** Kotlin + Jetpack Compose
- **Pattern:** MVVM + Repository
- **Persistence:** Room (relational), DataStore Preferences (key-value)
- **Audio:** Native `AudioTrack` + custom DSP (`BiquadFilter`) for **MASK** (narrow bandpass) and **NOTCH** (broadband notch) modes at the matched frequency. Future: notch _or_ amplify toggle, color noise, ambient layer mixer.
- **Background audio:** Foreground Service with media-style notification _(planned — currently AudioEngine is Activity-bound)_

## Folder / package layout

```
com.tinnitustracker/            ← actual package (not com.example.tinnitus)
├── TinnitusTrackerApp.kt   ← Application subclass; owns AudioEngine singleton ✓
│
├── audio/
│   ├── engine/             ← AudioEngine.kt (MASK + NOTCH DSP) ✓, BiquadFilter.kt ✓
│   ├── service/            ← TherapyAudioService ✓ + AudioNotification ✓
│   └── routing/            ← AudioFocusController ✓ (BecomingNoisy planned)
│
├── data/
│   ├── database/           ← AppDatabase ✓, entities/DiaryEntry ✓, dao/DiaryDao ✓
│   │                         (TFIAssessment, ListeningSession, SoundPreset entities planned)
│   ├── preferences/        ← PreferenceKeys.kt ✓
│   └── repository/         ← UserSettingsRepository ✓, DiaryRepository ✓, AudioRepository ✓
│                              AssessmentRepository (planned)
│
├── domain/                 ← pure-Kotlin business logic (planned — not yet created)
│
├── ui/
│   ├── home/               ← HomeScreen ✓ (stub; full design in Next #2)
│   ├── matcher/            ← FrequencyMatchingScreen + FrequencyMatchingViewModel ✓ (subscreen of 소리)
│   ├── sounds/             ← SoundSettingsScreen ✓ (stub; opens matcher subscreen — full expansion in Next #3)
│   ├── therapy/            ← TherapyScreen (planned)
│   ├── assessment/         ← TFIQuestionnaireScreen (planned)
│   ├── records/            ← RecordsScreen ✓ (stub; full design in Next #5)
│   ├── diary/              ← DiaryScreen (planned)
│   ├── onboarding/         ← OnboardingScreen ✓ (7-page tutorial; tinnitus → therapy → app guide)
│   ├── settings/           ← SettingsScreen.kt ✓ (placeholder + 튜토리얼 다시 보기)
│   └── theme/              ← Color.kt, Theme.kt, Type.kt ✓
│
└── MainActivity.kt         ← 4-tab scaffold (홈 / 소리 / 기록 / 설정) ✓; 소리 owns a Matcher subscreen
```

Folders are created lazily — only when a file actually lives there.

## State management — Single Source of Truth

UI state flows **Repository → ViewModel → Compose**. Repositories are the only layer that reads from or writes to Room or DataStore. The AudioEngine does not own user state; it observes Repository state and reflects it.

When the user updates the matched frequency, the change goes Repository → AudioEngine **and** Repository → UI simultaneously. No duplicated state.

## Data model — Room entities

Grouped by tier — see [[wiki/app/plan]].

**MVP / basic functions** (per handwritten plan in `raw/Plan(수기).md`)

| Entity | Purpose | Notable fields |
|---|---|---|
| `SoundPreset` | Saved sound mix configuration | id, name, processingMode (`notch`/`amplify`/`off`), colorNoise (`pink`/`white`/`brown`/`none`), colorNoiseGain, ambientLayers (list of `{source, gain}`), createdAt, lastUsedAt |
| `ListeningSession` | Auto-recorded playback session | id, startedAtEpochMs, endedAtEpochMs, durationMs, presetLabel? (planned to be superseded by per-segment labels — see below), colorNoise?, ambient?, activity? (mock label dimensions, 2026-05-14) |
| `ListeningSessionSegment` _(planned, [[plan]] Next #2)_ | Per-sound slice of a session — split on every `onSoundChanged` so mid-session sound switches preserve a per-sound time breakdown instead of collapsing to a single "dominant" label | id, sessionId (FK), soundId/presetLabel, startedAtEpochMs, durationMs |
| `TFIAssessment` | Tinnitus Functional Index — default bi-weekly | id, date, items[], totalScore, subscaleScores |
| `DiaryEntry` | Daily symptom + stress + free-text journal | id, date, severity, stressLevel, note |

**Advanced (Tier 1+)** — gated behind later tiers in [[wiki/app/plan]]

| Entity | Purpose | Notable fields |
|---|---|---|
| `MixingPointCalibration` | Per-ear calibrated dB level for [[wiki/clinical/sound-therapy]] | id, date, leftDb, rightDb |
| `SpikeEvent` | Logged tinnitus spikes | id, timestamp, trigger, durationSec, interventionUsed |
| `UserProgress` | TRT phase tracking | id, currentPhase, phaseStartDate, milestones |

Schema versioning via Room migrations. No destructive migrations on production data.

> **`ListeningSession` minimum-duration floor** — raised to **3 min (180 000 ms)** on 2026-05-14. Was 5 s in v1; 3 min is a deliberate compromise between the 5-s noise filter and Gemini's proposed 5-min policy filter (which would silently drop legitimate short TRT exposures — see [[wiki/sources/gemini-records-architecture-2026-05]]).

> **Assessment instrument note** — the handwritten plan (`raw/Plan(수기).md`) mentions a "THI 설문지". This codebase uses **TFI** (Tinnitus Functional Index) instead — newer, treatment-responsive, designed to detect change over time. THI and TFI are not interchangeable; TFI is the chosen primary outcome. The user-facing copy still uses the generic "이명 설문지" wording so the instrument can be referenced without confusion.

## DataStore preferences

**Implemented:**

| Key | Type | Purpose |
|---|---|---|
| `matched_frequency_hz` | Float | User's confirmed tinnitus pitch (default 1000 Hz) |
| `has_tonal_tinnitus` | Boolean? | `true` = pitch confirmed; `false` = skipped; `null` = undecided |
| `processing_mode` | String | `"notch"` / `"amplify"` at matched frequency (default `"notch"`) |
| `onboarding_complete` | Boolean | Gates the onboarding tutorial on first launch; written on Finish or Skip (default `false`) |

**Planned (not yet in PreferenceKeys.kt):**

| Key | Purpose |
|---|---|
| `active_preset_id` | Currently-selected `SoundPreset` |
| `sleep_timer_default_min` | Last-used sleep timer duration |
| `daily_listening_goal_min` | Calendar day-complete threshold (default 120 min) |
| `current_streak_days` | Consecutive days meeting the listening goal |
| `tfi_cadence_weeks` | TFI re-assessment cadence (`1` or `2`, default `2`) |
| `last_tfi_date` | Drives TFI re-assessment prompt |
| `tinnitus_explainer_seen` | Tutorial-mode explainer dismissed |
| `current_trt_phase` | _(Tier 1)_ Cached TRT phase |
| `last_red_flag_screen_date` | _(Tier 1)_ 30-day red-flag screener cadence |

DataStore holds **lightweight, frequently-read** values. Anything historical or relational (sessions, assessments, presets) lives in Room.

## Audio Foreground Service _(✓ landed 2026-05-11; see [[wiki/app/plan]] Done)_

Pattern: **Application-singleton engine + started-only foreground service**, chosen over the canonical bound-service pattern because `AudioEngine` has no `Context`-dependent state and the ViewModel's StateFlow exposures need a stable non-null reference.

- `TinnitusTrackerApp : Application` owns the `AudioEngine` as a `by lazy` singleton — engine lives as long as the process does.
- `TherapyAudioService` is `started` only (no `onBind`). Two actions: `ACTION_START` (calls `startForeground` with the notification) and `ACTION_STOP` (engine.stop + `stopForeground(REMOVE)` + `stopSelf`). Manifest declares `foregroundServiceType="mediaPlayback"` and `exported="false"`.
- `AudioRepository` is the single point of UI access. It forwards the engine's StateFlows directly (`val frequency = engine.frequency`, etc.) so the ViewModel's read pattern is unchanged. `start()` calls `ContextCompat.startForegroundService` then `engine.start()`; `stop()` calls `engine.stop()` then `context.stopService(...)`.
- `AudioNotification` builds the `NotificationCompat` with a single "정지" action; content tap returns to `MainActivity` (declared `launchMode="singleTop"`).
- Architecture rule preserved: `ui → AudioRepository → engine`. ViewModels never reference `AudioEngine` directly.

**Out of scope of this slice** (separate Next items): occluding-earphone route detection (BECOMING_NOISY); lock-screen / MediaSession integration; resumable-pause state in the notification.

## Audio focus & interruption handling _(✓ landed 2026-05-11; see [[wiki/app/plan]] Done)_

`audio/routing/AudioFocusController` owns a single `AudioFocusRequest` (USAGE_MEDIA / CONTENT_TYPE_MUSIC; `setWillPauseWhenDucked(true)` — we pause instead of letting the system duck therapeutic noise). The controller's own listener mirrors LOSS / GAIN into its `hasFocus` flag so re-requesting after a permanent loss actually re-asks the system.

`AudioRepository` is the policy layer:
- `start()` requests focus before promoting the service / starting the engine.
- `stop()` (user-initiated) abandons focus and tears down the service.
- `LOSS` → full teardown; clears the `wasPlayingBeforeInterruption` latch.
- `LOSS_TRANSIENT` / `LOSS_TRANSIENT_CAN_DUCK` → engine off, **service stays in foreground**, latch set.
- `GAIN` after a transient → engine restarts; latch cleared.

## Module dependency rules

```
ui  →  domain  →  data
ui  →  audio  (only via repository)
audio/service  →  audio/engine
data/repository  →  data/database  +  data/preferences
```

- `ui` never touches `data` directly — always through a Repository
- `audio/engine` has no Compose / Activity dependencies (testable in isolation)
- `domain` is pure Kotlin (no Android imports) — easy to unit test

## See also
- [[wiki/app/prd]] — product scope
- [[wiki/app/plan]] — implementation sequence
- [[wiki/app/android-snapshot]] — current-state snapshot of code
