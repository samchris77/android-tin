# Architecture Overview

## UI Structure

### Navigation
- **4-tab bottom navigation** (light/cream, teal active, muted inactive, TealSoft indicator)
  - 홈 (Home)
  - 소리 (Sound)
  - 기록 (Records)
  - 설정 (Settings)
- **Matcher subscreen** — opens within 소리 tab, dark background (expected visual seam)

### Design System
- **Material 3** with custom light color scheme
- **Primary:** Teal (`#0A8E8C`)
- **Secondary:** Coral (`#FF8060`)
- **Background:** Cream (`#FFFAF5`)
- **Surface:** White (`#FFFFFF`)
- **Status bar:** Light appearance (dark icons on cream background)

### Screens

#### 홈 (Home) — ✓ (Direction A + D week strip, 2026-05-12)
- **Top bar:** Greeting + treatment week + settings gear icon
- **Hero card:** Gradient background, therapy preset display ("저녁 휴식"), play button, listen-time overlay (84/120 min)
- **Week strip:** 7-column progress with past-days checkmarks, today's gradient bar, future-days empty
- **Sleep timer chip:** Hardcoded "30분" (no-op wiring)
- **TFI prompt card:** Hardcoded content (no-op wiring)

#### 소리 (Sound) — Pending redesign
- Currently: dark background + orange active, matches old matcher chrome
- Next: migrate to light/teal/coral per visual system

#### 기록 (Records) — Pending redesign
- Currently: "준비 중" stub
- Planned: calendar view + listening log + TFI trend chart

#### 설정 (Settings) — Pending redesign
- Currently: placeholder sections (learning, listening goals, frequency, info)
- Next: style update to light/teal/coral

#### 통일 (Matcher) — Unchanged
- Dark background (intentional — legacy matcher UX)
- Sits inside 소리 tab as a subscreen
- Light tab bar at bottom (expected visual seam during use)

## Data Layer

### Room Database
- Tables: therapy sessions, frequency history, TFI responses (scaffold in place)
- Not yet fully wired to screens

### DataStore
- Persists: user frequency selection, processing mode (notch/broadband)
- Readable by all screens without rebuilds

### Audio
- `AudioRepository`: manages playback, therapy presets, notching/broadband modes
- `ForegroundAudioService`: handles audio focus, interruptions, remote controls

## Known Hardcodes & TODOs

- 홈 top bar: "치료 3주차" (treatment week) — hardcoded literal, needs wiring to actual treatment start date
- 홈 hero: "저녁 휴식" preset — fully hardcoded; real preset switching pending
- 홈 sleep timer: 30분 hardcoded; no-op on tap
- 홈 TFI prompt: hardcoded "이번 주 설문조사" text; no questionnaire flow yet
- 기록 week strip: hardcoded metrics ("일평균 98분", "+12분", "7일 연속") — not fetched from DB yet
- Therapy play: button routes to 소리 tab; doesn't auto-start playback (user must open matcher)

## Build Configuration

- `minSdk = 28` (Java 8 time APIs via desugaring)
- Compose + Material 3
- Gradle 8.x
- Kotlin 1.9+
