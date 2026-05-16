---
title: "Android Tinnitus Tracker - Current State Snapshot"
type: app
aliases: ["Android App Summary", "Code Snapshot"]
tags: [app/architecture]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-11
---

# Android Tinnitus Tracker — Current State Snapshot

_Snapshot as of 2026-05-11. For the target architecture, see [[wiki/app/architecture]]. For the implementation roadmap, see [[wiki/app/plan]]._

## Package & build

- **Application ID / package:** `com.tinnitustracker`
- **Language / UI:** Kotlin + Jetpack Compose
- **Audio:** Native `AudioTrack` + custom `BiquadFilter` (biquad DSP)
- **Persistence:** DataStore Preferences only — Room database not yet wired up

## What's built (11 Kotlin files)

### Entry point

**`MainActivity.kt`** — Two-tab scaffold: **Matcher** (톤 찾기) and **Settings** (설정). `AudioEngine` and `UserSettingsRepository` are instantiated here and passed down. `AudioEngine.stop()` called on `onPause`; `release()` on `onDestroy`. No foreground service yet — audio stops when the app backgrounds.

### Audio layer

**`audio/engine/AudioEngine.kt`** — Real-time DSP on a background thread. Two modes:
- `MASK` — narrow bandpass noise centered on the matched frequency (high Q = 10); used for pitch matching and in-the-moment relief.
- `NOTCH` — broadband white noise with a wide notch carved out (low Q = 0.4, ~3 octaves); used for notched-sound therapy that drives lateral inhibition around the tonal frequency.

Smoothed frequency transitions (α = 0.1) prevent clicks when the dial moves. Tanh soft-clipping protects hearing. Frequency range: 100 Hz – 16 kHz. Volume hard-capped in the ViewModel at 70 %.

**`audio/engine/BiquadFilter.kt`** — Single biquad filter; supports bandpass and notch topologies. Stateful — must be reset when topology changes (`filter.reset()` on mode switch).

### Data layer

**`data/preferences/PreferenceKeys.kt`** — Three DataStore keys currently live:

| Key | Type | Purpose |
|---|---|---|
| `matched_frequency_hz` | Float | User's confirmed tinnitus pitch |
| `has_tonal_tinnitus` | Boolean? | `true` = tonal pitch confirmed; `false` = user skipped; `null` = not yet decided |
| `onboarding_complete` | Boolean | Not yet used in UI; key present for future onboarding gate |

**`data/repository/UserSettingsRepository.kt`** — Wraps the DataStore. Exposes `matchedFrequencyHz: Flow<Float>` and `hasTonalTinnitus: Flow<Boolean?>`. Writes: `saveMatchedFrequency(hz)`, `setHasTonalTinnitus(value)`.

### UI layer

**`ui/matcher/FrequencyMatchingScreen.kt`** — The main working screen. Key components:
- **Logarithmic dial** (100 Hz – 16 kHz, log scale). Drag-to-rotate with haptic ticks every 6°. Displays Hz value and Korean pitch-band label (저음 / 중저음 / 중음 / 고음 / 초고음).
- **Stepper buttons** — ±10 and ±100 Hz; all values snap to nearest 10 Hz.
- **Volume safety card** — slider hard-capped at 70 %; central play/stop button.
- **Octave confusion card** — slides up (animated) after 2 s of dial inactivity. Lets user audition the current frequency one octave down, at pitch, or one octave up. Confirm saves to DataStore. Auto-scrolls into view.
- **Skip affordance** — "뚜렷한 음을 찾기 어렵나요?" text button; sets `has_tonal_tinnitus = false` via `skipTonalPitch()`.

**`ui/matcher/FrequencyMatchingViewModel.kt`** — Delegates audio state to `AudioEngine` flows. Owns octave-check timer (2 s `delay` in a `viewModelScope` coroutine, cancelled on each dial interaction). `confirmPitch()` saves frequency + sets tonal flag. `setVolumeSafe()` enforces 70 % cap. Logarithmic slider ↔ Hz conversion utilities.

**`ui/settings/SettingsScreen.kt`** — Placeholder. Displays version (1.0), sample rate (44.1 kHz), output (Mono). No functional settings yet.

**`ui/theme/`** — Dark background (`DarkBg`), cream knob panel, orange accent (`OrangeAccent`). Material 3 theme. Night-use optimized.

## What's NOT yet built

- **Room database** — no entities wired up; `DiaryEntry`, `TFIAssessment`, `ListeningSession`, `SoundPreset` exist only in the architecture plan
- **Foreground audio service** — audio stops on Activity pause
- **Therapy / sound settings screen** — no notch/amplify toggle, no color-noise picker, no ambient layers
- **TFI questionnaire** — not started
- **Records / calendar page** — not started
- **Onboarding / explainer** — `onboarding_complete` key exists but nothing reads it yet
- **Audio focus handling** — no `AudioManager` integration
