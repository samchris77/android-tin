---
title: "Implementation Done — Archive"
type: app
aliases: ["Done", "Plan Done", "Completed Work"]
tags: [app/planning, app/archive]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-19
---

# Implementation Done — Archive

Full write-ups of completed implementation tasks. Moved out of [[wiki/app/plan]] on 2026-05-19 to keep the plan file actionable. Sorted newest-first.

---

## ✓ App fixes round 2 — 2026-05-19

17 sub-items across 5 screens (Home / 소리 / 기록 / 설정 / MiniPlayer) captured from the 2026-05-19 user walkthrough (two annotated screenshots). Shipped as one bundle since most items shared plumbing (sleep-timer lift, goal Flow propagation, MiniPlayer signature change). Build green; `:app:compileDebugKotlin BUILD SUCCESSFUL`.

**Foundation.**
- `PreferenceKeys.PROCESSING_MODE` doc updated: valid values now `"notch" | "amplify" | "off"`.
- `AudioEngine.Mode` enum extended with `OFF`. In `generate()`: skip `filter.process()` and zero out the frequency-domain gain when `Mode.OFF`; color noise + ambient mix continue. `filter.reset()` still runs on transition to/from OFF (existing click-suppression behavior).
- `AudioRepository` `activePresetId` observer now maps `preset.processingMode == "off" → Mode.OFF` alongside the existing `"amplify" → MASK` / else → NOTCH branches.
- **Sleep-timer lift:** `AudioRepository.SleepTimerState(setMinutes, remainingSec)` data class + `sleepTimer: StateFlow<SleepTimerState?>` + `setSleepTimer(min)` / `cancelSleepTimer()` APIs. Countdown coroutine runs on `ioScope`. On expiry: clear state + call `pause()` (session stays open). `HomeViewModel` now delegates `setSleepTimer` / `cancelSleepTimer` to the repo and exposes `audioRepo.sleepTimer` directly — single source of truth for Home card + MiniPlayer.

**Home (`HomeScreen.kt`).**
- Settings gear icon removed (overlapped with bottom nav 설정 tab).
- Hero card preset name made tappable via new `onOpenPresetPicker` callback → `PresetPickerBottomSheet` (new file `ui/home/PresetPickerBottomSheet.kt`). Sheet lists all presets from `SoundPresetRepository.observeAll()`; tap commits `UserSettingsRepository.setActivePresetId(id)`. Active preset highlighted with TealSoft bg + Teal `Check` icon.
- Sleep timer card copy: `"${set}분 설정 · ${remainingMinutes}분 남음"` when active (vs. just `"꺼짐"` previous).
- MainActivity: `MiniPlayer.visible = current != Tab.Home && !(current == Tab.Sound && soundSub == SoundSub.Matcher)` — hidden on Home (hero card is the sole player there), preserved on 소리/기록/설정.

**소리 (`SoundSettingsScreen.kt`).**
- `FrequencyCard` + `ProcessingModeCard` merged into one `FrequencyAndProcessingCard` (`Radius.card` shape, single border + shadow). Layout: frequency row + 다시 측정 pill | thin divider | master toggle row | segmented control | explainer caption.
- New master `Switch("주파수 처리 사용")` above the segmented selector. Off → `onModeChanged("off")` → `PROCESSING_MODE = "off"`. Segments wrap in `Modifier.alpha(0.4f)` and intercept-click-when-off `Box` so they read as disabled without rebuilding.
- `ColorNoiseCard`: `끄기` pill + `듣기 테스트` button both removed. Three pills only (핑크 / 화이트 / 브라운). Re-tap on selected pill calls `onColorChanged("off")` → deselects.

**기록 (`RecordsScreen.kt` + `RecordsViewModel.kt`).**
- Spillover-month day cells (`!cell.inCurrentMonth`): bg = `Color.Transparent` instead of `Heat0`. Numerals stay `Muted`.
- Calendar nav `<` / `>` chevrons: tint changed `Ink2` → `Teal` for visibility.
- `WeeklySummarySection` call site commented out (`// 주간 요약 hidden 2026-05-19 — re-enable when design is reworked`). `WeekSummary` data class + `buildWeekSummaries` + `RecordsUiState.weeklySummaries` left intact; orphaned flow is harmless.
- `heatColor` normalization changed: was `(totalMs / maxDayMs)` (in-month max), now `(totalMs.toFloat() / dailyGoalMs)` capped at 1.0 (goal-relative). `RecordsViewModel` constructor accepts `UserSettingsRepository`; `dailyGoalMin` Flow surfaced on `RecordsUiState`; `RecordsViewModelFactory` updated. Result: identical activity now buckets differently when the user changes their daily goal from 2시간 to 6시간.

**설정 (`SettingsScreen.kt` + `MainActivity.kt`).**
- New 치료 시작일 row (`ChevronRow` style) — backed by `TREATMENT_START_DATE`; tap opens Material 3 `DatePickerDialog`; subtitle formats `2026년 5월 11일` style. Drives Home `n주차` label via existing `HomeViewModel.treatmentStartDate` Flow.
- Disabled `일일 청취 목표` `ValueRow` replaced with inline `SegmentedControl` (3 options: 2시간 → 120, 4시간 → 240, 6시간 → 360 min). Backed by `DAILY_LISTENING_GOAL_MIN`. Propagates to Home adherence ring + Records heatmap bucketing.
- `ActionRow` migrated to `ChevronRow` from `ui/theme/components/`. `TfiCadenceRow` value wrapped in `Pill(variant = TealSoft)`. 청취 목표 section header removed (rows self-labeling). 학습 자료, 주파수, 정보 headers kept (info groupings benefit).
- MainActivity threads `treatmentStartDate`, `dailyGoalMin` Flows + `onSetTreatmentStartDate` / `onSetDailyGoal` setters via `UserSettingsRepository`.

**Player (`MiniPlayer.kt`).**
- Signature: added `liveSession: LiveSession?`, `sleepTimerRemainingSec: Int?`, `onLogSession: () -> Unit`.
- Elapsed counter: 1 Hz `LaunchedEffect(liveSession?.startedAtEpochMs)` ticks `(now - startedAt) / 1000` formatted `MM:SS`. Freezes when `liveSession.isPaused`.
- Sleep timer indicator: when `sleepTimerRemainingSec != null`, renders `수면 MM:SS` alongside elapsed. Hidden otherwise.
- New circular `세션 기록` button (Material `Icons.Filled.EditNote`) next to play/pause. Tap → `onLogSession()` → `MainActivity` sets `quickLogPrefillPreset = activePreset?.name` + `showQuickLog = true`. Shared `QuickLogBottomSheet` host in MainActivity pre-fills the active preset name as a tag.

**Files.** Modified: `data/preferences/PreferenceKeys.kt`, `audio/engine/AudioEngine.kt`, `data/repository/AudioRepository.kt`, `ui/home/HomeViewModel.kt`, `ui/home/HomeScreen.kt`, `ui/sounds/SoundSettingsScreen.kt`, `ui/records/RecordsViewModel.kt`, `ui/records/RecordsScreen.kt`, `ui/settings/SettingsScreen.kt`, `ui/components/MiniPlayer.kt`, `ui/matcher/FrequencyMatchingScreen.kt`, `MainActivity.kt`. New: `ui/home/PresetPickerBottomSheet.kt`.

**Verified 2026-05-19 (build):** `./gradlew :app:compileDebugKotlin` BUILD SUCCESSFUL in 663ms. Emulator walk-through pending — flagged as the only outstanding verification step.

**Not in this task:** MiniPlayer haptic feedback on session-record tap; long-press menu on the elapsed counter to drill into the session; per-segment color noise mid-session.

---

## ✓ MVP Bug Fix + Emulator Verification — 2026-05-16

Static analysis + Gradle build surfaced 3 compile errors introduced during MVP implementation. All fixed; full emulator walkthrough passed.

**Fixes.**
- `SurfaceColor` → `Surface` (7 callsites in `HomeScreen.kt` — token didn't exist; correct name is `Surface` from `Color.kt`).
- Smart-cast failure on `sleepTimerRemaining` (nullable StateFlow delegate) → captured as local `val remaining` before the `if`-check.
- `audioRepo.play()` called in `HomeViewModel.togglePlay()` and `SoundSettingsViewModel.togglePreview()` — method doesn't exist on `AudioRepository`; replaced with `audioRepo.togglePlay()` which internally handles start vs. resume.

**Streak counter removal (user direction).** Removed `consecutiveDays: Int` field from `HomeUiState`, the comment block and `consecutiveDays = 0` assignment in the `combine` lambda, and the `🔥 N일 연속` `Surface` badge from `HomeScreen.kt`. Treatment-week label (`치료 N주차`) kept.

**Emulator verification on emulator API 36.1** — BUILD SUCCESSFUL (warnings only: unused param, deprecated `capitalize`, opt-in annotation). APK installed. Screens verified:
- Home: adherence ring, treatment-week label, no streak badge.
- Sleep Timer bottom sheet: 15 / 30 / 60 / 90분 options + 타이머 끄기.
- Quick Log bottom sheet: tinnitus + stress sliders, 6 tag chips, 저장 CTA.
- Records: calendar, weekly summaries (5.10–16 card shows real data), 최근 기록 list with "..." options button → EntryOptionsBottomSheet ("기록 옵션" + "삭제" in red).

**Files.** Modified: `ui/home/HomeViewModel.kt`, `ui/home/HomeScreen.kt`, `ui/sounds/SoundSettingsViewModel.kt`.

---

## ✓ MVP Completion (홈, 소리, 기록 Polish) — 2026-05-16

Executed the final wiring and UI integration to complete the MVP, fully applying the "Player-First" Light Theme redesign.

**Home Screen Wiring:**
- Implemented `HomeViewModel` to aggregate `todayListenMs`, `dailyGoalMin`, and `treatmentWeek`.
- Fully replaced `HomeScreen.kt` with the high-fidelity Compose layout based on `home_prototype.html`.
- Features circular progress play button, live adherence card, Sleep Timer bottom sheet (15/30/60/90 mins), and Quick Log integration.

**Audio Engine & Sound Settings:**
- Loaded `brook.mp3` and `fireplace.mp3` into `AudioEngine` and exposed them as sliders in the Ambient Mix card.
- Implemented `saveAsNewPreset` functionality via a "새 프리셋 저장" dialog on the "+ 저장" button.
- Wired the "미리듣기" (Preview) button to toggle `AudioEngine` playback without committing to the database.

**Records Polish:**
- Added `deleteSession` and `deleteDiary` methods to `RecordsViewModel`.
- Implemented `EntryOptionsBottomSheet` allowing users to delete individual log entries from the Recent Entries list.

---

## ✓ 주간 요약 (Weekly summary) cards on 기록 — 2026-05-16

Replaced `WeeklySummaryPlaceholder` ("준비 중") with real per-week aggregate cards anchored to the visible month. Cards are Sunday-anchored, reusing `buildGridDates(ym).chunked(7)`; weeks filtered to those containing at least one day in `visibleMonth`. Aggregation lives in `RecordsViewModel` (no new DAO queries), consistent with the existing heatmap pattern. Visual spec derived from `ux-principles.md` + component library (no `IMG_2486.PNG` — not present in `raw/`).

**Data.** New `WeekSummary` data class: `startDate`, `endDate`, `tinnitusAvg: Float?` (null = no diary entries that week), `stressAvg: Float?`, `listenMs: Long`, `daysWithData: Int`. `RecordsUiState` gained `weeklySummaries: List<WeekSummary>`. `assemble()` now takes diaries as a second arg; state flow changed from `sessionFlow.map` to `combine(sessionFlow, diaryFlow)`. `buildWeekSummaries()` added to `RecordsViewModel`.

**UI.** `WeeklySummarySection` — `SectionHeader("주간 요약")` + column of cards. `WeeklySummaryCard` — week label + day-count `Pill(TealSoft)` + three `MetricRow`s (이명 평균 / 스트레스 평균 / 청취 시간). Missing metric renders `"—"` in Muted; empty week (all zeros) renders `WeeklySummaryEmptyCard` ("데이터 없음" + week label). Month-boundary weeks show both month prefixes in the label (e.g. `"10.27 – 11.02"`). Format helpers: `formatWeekLabel`, `formatScoreValue`, `formatListenValue`.

**Files.** Modified: `ui/records/RecordsViewModel.kt`, `ui/records/RecordsScreen.kt`.

**Verified 2026-05-16 on emulator-5554**: BUILD SUCCESSFUL in 4 s. APK installed (`Performing Streamed Install ... Success`).

---

## ✓ State + interactive-feedback audit — 2026-05-16

Final hygiene sweep across all `ui/` screens after the 1–4 polish-pass foundations.

**5A — Forbidden-literal cleanup.** Snapped all `5 / 6 / 9 / 10 / 14 / 18 / 22 / 26 / 28 dp` literals to `Spacing.*` / `Radius.*` / `Elevation.*` tokens in: `SettingsScreen`, `HomeScreen`, `RecordsScreen`, `QuickLogBottomSheet`, `DayDetailBottomSheet`, `SoundSettingsScreen`, `OnboardingScreen`, `TfiQuestionnaireScreen`, `TfiResultsScreen`. Documented exceptions: `ui/theme/` (token authority), `FrequencyMatchingScreen` (bespoke metallic-knob geometry), `OnboardingScreen` drawScope wave / brain-network / checkmark geometry, 3 dp MCID legend swatch in `RecordsScreen`, 1.5 dp shadow elevations in `SettingsScreen`.

**5B — Korean overflow hardening.** Added `maxLines + overflow = TextOverflow.Ellipsis` to 5 dynamic `Text` callsites that could receive long Korean strings: preset name in `SoundSettingsScreen` (1-line), TFI item prompt in `TfiQuestionnaireScreen` (3-line), session preset subtitle in `DayDetailBottomSheet` (1-line), diary note composite in `DayDetailBottomSheet` (2-line), recent-entry subtitle in `RecordsScreen` (1-line).

**5C — Seed-and-walk.** Static audit confirmed 0 `.clickable {` violations outside `TabRow`/`Slider`, 0 non-`Icons.Filled.*` imports. Manual visual walk with long preset seed deferred to user; no blocker for shipment.

**Files.** Modified: `ui/settings/SettingsScreen.kt`, `ui/home/HomeScreen.kt`, `ui/records/RecordsScreen.kt`, `ui/records/QuickLogBottomSheet.kt`, `ui/records/DayDetailBottomSheet.kt`, `ui/sounds/SoundSettingsScreen.kt`, `ui/onboarding/OnboardingScreen.kt`, `ui/assessment/TfiQuestionnaireScreen.kt`, `ui/assessment/TfiResultsScreen.kt`.

**Verified 2026-05-16 on emulator-5554**: BUILD SUCCESSFUL in 9 s. APK installed. Grep clean: 0 forbidden-literal hits outside documented exceptions.

---

## ✓ Design tokens + shared component library — 2026-05-14

Phase 1 of the app-wide design overhaul. Establishes the foundation that the next five queued items (홈 / 소리 / 기록 / 설정 polish + interactive-feedback audit) all consume. Driven by user-supplied canonical inputs: [[wiki/sources/kole-jain-beginner-mistakes]] + [[wiki/sources/kole-jain-genius-ux]] + [[ux-principles]]. The 2026-05-14 audit found drift across the four primary screens (card radii 10 / 12 / 16 / 22 dp scattered; tag pills hand-built in 3 places; `TfiPromptCard` "시작 →" arrow re-implementing `PolishedPlayButton`'s gradient and shadow; `SettingsScreen` rows still on plain `.clickable`). This item ships the components; items 2–6 wire them in screen-by-screen.

**Tokens.** New `ui/theme/Tokens.kt` with three objects:
- `Radius` — `card = 16.dp` (up from drift'd 12 dp), `chip = 8.dp`, `button = 12.dp`, `pill = 100.dp`.
- `Spacing` — `xs / sm / md / lg / xl / xxl / section = 4 / 8 / 12 / 16 / 20 / 24 / 32 dp` (strict 4 dp grid; forbidden literals after this pass: 5, 6, 9, 10, 14, 18, 22, 26, 28).
- `Elevation` — `card / raised / hero = 2 / 4 / 8 dp`.

**Component library.** New `ui/theme/components/` directory with 10 composables, each with an `@Preview`:
- `AppCard` — single card primitive with 5 variants (`Default`, `TealSoft`, `CoralSoft`, `DarkHero`, `DarkNight`). Pulls shape + shadow + padding into one place.
- `PrimaryButton` — Teal h44 r12 with `pressableClickable`, supports `enabled` + `loading`.
- `SecondaryButton` — ghost h44 r12, transparent + 1 dp Line border.
- `Pill` — 4 variants (`Teal`, `TealSoft`, `CoralSoft`, `Outline`) × 2 sizes (`Small` 24 dp, `Medium` 28 dp), optional leading/trailing icons, optional `onClick` (renders as static badge if null).
- `TagChip` — multi-select toggle, 32 dp tall, 8 dp radius, outlined → filled-soft on selection.
- `SegmentedControl` — per wireframe `.seg`: 4 dp outer padding, 4 dp gap, 8 dp inner radius, subtle spot shadow on the active segment.
- `IconBadge` — square tinted tile + icon (40 dp default), `Teal` / `Coral` variants.
- `ChevronRow` — Material list row: leading slot, title, optional subtitle, optional trailing slot, optional chevron, `pressableClickable` press feedback.
- `EmptyStateCard` — centered Muted text + optional icon + optional subtext + optional CTA slot. Wraps an `AppCard` so empty states share the same shape language.
- `SectionHeader` — 17 sp bold title + optional trailing slot (e.g. a `Pill` legend badge).

**PolishedPlayButton extension.** Added `PlayButtonVariant.Teal` to the existing enum (alongside `Coral` + `Orange`) — gradient `0xFF4F8A87 → Teal → 0xFF1F4A48`, Teal 55 % spot shadow, 10 dp rest elevation. Used in item 2 to replace `TfiPromptCard`'s hand-rolled "시작 →" arrow.

**ux-principles.md §5 update.** Spec changed from "12 dp cards, 8 dp chips" to "16 dp cards / 8 dp chips / 12 dp buttons / 100 dp pills" with the rationale captured inline (wireframe HTML `--r-card: 20px` vs principles; we land at 16). Forbidden-spacer list added so future "polish" doesn't smuggle 10 / 14 dp back in. New bullet documents the `ui/theme/components/` location and the rule "adding a new variant means extending an enum, not building a parallel component."

**Files.** New: `ui/theme/Tokens.kt`, `ui/theme/components/AppCard.kt`, `ui/theme/components/PrimaryButton.kt`, `ui/theme/components/SecondaryButton.kt`, `ui/theme/components/Pill.kt`, `ui/theme/components/TagChip.kt`, `ui/theme/components/SegmentedControl.kt`, `ui/theme/components/IconBadge.kt`, `ui/theme/components/ChevronRow.kt`, `ui/theme/components/EmptyStateCard.kt`, `ui/theme/components/SectionHeader.kt`. Modified: `ui/theme/PressEffect.kt`, `Tinnitus_obsidian/wiki/app/ux-principles.md`.

**Verified 2026-05-14 on emulator-5554**: Build green; APK installed cleanly; cold launch sanity check passes; no visual change (components ship unused this item, which was the goal).

---

## ✓ Day-tap drilldown on 기록 calendar — 2026-05-14

Every day cell on the heatmap is now tappable. Tap opens a `DayDetailBottomSheet` showing all `ListeningSession` rows (start/end clock, duration, preset label) and `DiaryEntry` rows for that local date — sessions and diaries grouped under labeled cards, newest-first within each group. Empty days surface "이 날에는 기록이 없어요". Spillover days from neighboring months are tappable too.

**Data layer.** `RecordsViewModel` gained a `selectedDay: MutableStateFlow<LocalDate?>` and a derived `dayDetail: StateFlow<DayDetail?>` that `flatMapLatest`-es into `combine(sessionRepo.observeRange(dayStart, dayEnd), diaryRepo.observeRange(...))` keyed on local-zone start-of-day → start-of-next-day bounds. New `DayDetail(date, sessions, diaries)` data class with `isEmpty` / `totalListenMs` derived properties. Public `selectDay(LocalDate?)` opens (non-null) and dismisses (null) the sheet.

**UI.** New `ui/records/DayDetailBottomSheet.kt` — Material 3 `ModalBottomSheet`, header `M월 D일 (요일)` + summary subtitle. Two stacked white cards: 청취 세션 + 일지. `RecordsScreen` collects `dayDetail` + hosts the sheet; `DayCellView` is now `pressableClickable`, threading `cell.date` up to `vm.selectDay(...)`.

Files: new `ui/records/DayDetailBottomSheet.kt`. Modified: `ui/records/RecordsViewModel.kt`, `ui/records/RecordsScreen.kt`.

**Verified 2026-05-14 on emulator-5554**: Build green; sheet renders correctly with sessions + diaries for today; back gesture dismisses cleanly.

---

## ✓ TFI history trend on 기록 — 2026-05-14

Replaces the `TfiTrendPlaceholder` "준비 중" card with a real `TfiTrendCard`: a Compose `Canvas` sparkline plus a header trend summary, sitting above the existing `이전 검사` table inside the TFI 점수 tab.

**Sparkline.** Plots scores chronologically (oldest → newest left-to-right). Y-axis fixed 0–100 with three implied gridlines. The latest score's ±13 MCID band is rendered as a translucent `TealSoft` rectangle behind the line. Polyline is 3 px Teal. Each data point draws a dot — coral halo for any pair-to-pair Δ ≥ +13 (worsening), green-teal for Δ ≤ −13 (improvement), Muted otherwise. The latest point gets a larger filled-Teal dot with a white inner pip.

**Header / legend.** Title row keeps `TFI 추이` + the existing `최근 N점` Teal pill. Subtitle reads `N 회 기록 · 첫 검사 대비 ↑X점 (악화)` / `↓X점 (호전)` / `변화 없음` based on the first-vs-latest delta and the MCID threshold. Below the canvas: a `TealSoft` swatch + `MCID 13점 — 임상적으로 유의미한 변화` legend line.

Files: modified `ui/records/RecordsScreen.kt`.

**Verified 2026-05-14 on emulator-5554** with two existing TFI rows (51 and 66): renders correctly with +15 delta worsening halo, MCID band centered around 66.

---

## ✓ Global Mini Player & True Pause — 2026-05-14

Replaced the `LiveSessionPill` (only visible during active sessions) with a persistent `MiniPlayer` bar above the bottom nav, always visible except during onboarding and the Frequency Matcher subscreen. Dark-themed card styled after `app-wireframes.html` Direction A hero player. Displays the active preset name and a circular Play/Pause toggle.

**True pause capability.** Added `pause()` and `resume()` to `AudioRepository`. Pausing stops the engine audio but keeps the foreground service alive and the listening session open — resuming continues the same session. `TherapyAudioService` now handles `ACTION_PAUSE` / `ACTION_RESUME` intents. `AudioNotification` dynamically swaps its action button between "일시정지" and "재생" based on `isPlaying` state.

Files: new `ui/components/MiniPlayer.kt`. Deleted `ui/components/LiveSessionPill.kt`. Modified: `data/repository/AudioRepository.kt`, `audio/service/TherapyAudioService.kt`, `audio/service/AudioNotification.kt`, `MainActivity.kt`.

---

## ✓ Per-sound time tracking on `ListeningSession` — 2026-05-14

Introduced a `ListeningSessionSegment` child table to break down `ListeningSession` tracking. The `AudioRepository` now tracks the exact duration of each active preset during a session and writes them to the database in a single transaction upon finalization.

---

## ✓ Preset Audio Engine & Sound Settings UI — 2026-05-14

Untangled the `AudioEngine` dependency knot blocking session analytics, introducing a robust `SoundPreset` system and real procedural audio synthesis. Replaces the mock labels from the previous additive migration.

**Data layer.** New `SoundPreset` entity (`id, name, processingMode, colorNoise, colorNoiseVolume, ambientMix`). Room v4 → v5 additive migration (`CREATE TABLE IF NOT EXISTS sound_presets`, inserts default '저녁 휴식' preset). `SoundPresetDao` and `SoundPresetRepository`. New DataStore key `ACTIVE_PRESET_ID` managed by `UserSettingsRepository`.

**Engine Extension.** `AudioEngine` extended with:
- Procedural color noise (Pink via simple 1-pole lowpass, Brown via deeper lowpass, White).
- Synchronized `MediaPlayer` instances for ambient background tracks (`rain`, `beach`), loaded via Context.

**Architecture Integration.** `AudioRepository` updated to consume `UserSettingsRepository` and `SoundPresetRepository`. On `init`, observes `activePresetId` and dynamically updates the engine (`setMode`, `setColorNoise`, `setAmbientMix`). The session logger now captures the *real* `currentPresetName` instead of mock randomized strings.

**UI Wiring.** `SoundSettingsScreen` components (processing mode, color noise pills, ambient sliders) are now driven by a new `SoundSettingsViewModel`. The "Auto-save to active preset" UX pattern allows changes to immediately persist to the active preset, deferring complex create/delete UI to a later iteration.

---

## ✓ FAB + Quick-Log Bottom Sheet for `DiaryEntry` — 2026-05-14

First entry UI for the `DiaryEntry` table (entity has existed since the Room scaffold; until now there was no way to write to it). A circular Teal FAB anchored at the bottom-right of the 기록 screen — visible across both tabs — opens a Material 3 `ModalBottomSheet` with no text input: only two sliders + chip toggles. Source: [[wiki/sources/gemini-records-architecture-2026-05]] § Aligned ideas.

**Sheet content** (`ui/records/QuickLogBottomSheet.kt`):
- Header: "기록 추가" 20 sp bold + muted subtitle "지금 상태를 빠르게 기록하세요".
- Two `SliderRow` cards (white surface, 12 dp radius): `이명 크기` and `스트레스`, each 0–10 step-snapped, bold Teal current value on the right.
- `태그` section: `FlowRow` of six `TagChip` toggles (`안도 / 악화 / 수면 / 스트레스 / 휴식 / 집중`). Selected = `TealSoft` bg + `Teal` border + `Teal` text + semibold; unselected = `Surface` bg + `Line` border + `Ink2`.
- Full-width Teal `저장` CTA.

**Persistence.** New `RecordsViewModel.addDiaryEntry(severity, stressLevel, tags)` wraps `diaryRepo.upsert(...)`. v1 packs the selected tags into the existing `DiaryEntry.note` column comma-separated — no schema migration. The Recent Entries List subtitle renders the packed tags by splitting on `","` and joining with ` · `.

Files: new `ui/records/QuickLogBottomSheet.kt`. Modified: `ui/records/RecordsScreen.kt`, `ui/records/RecordsViewModel.kt`.

**Verified 2026-05-14 on emulator-5554**: FAB renders; sheet animates up; tag selection + save adds row to Recent Entries with bullet-separated display.

---

## ✓ `LiveSessionPill` floating in-app tracking indicator — 2026-05-14

A small animated Teal pill anchored above the bottom nav, visible only while `AudioRepository` has an open session. Covers the in-app counterpart of the system-tray notification. Reference implementation: `raw/gemini_chat/gemini-code-1778723986116.kt`.

**Data layer.** `AudioRepository` now exposes a `LiveSession(startedAtEpochMs, isPaused)` data class via `liveSession: StateFlow<LiveSession?>`. State transitions:
- `start()` → `_liveSession.value = LiveSession(now, isPaused = false)`
- `LOSS_TRANSIENT` → `update { it?.copy(isPaused = true) }` (session stays open)
- `GAIN` after transient → `update { it?.copy(isPaused = false) }`
- `stop()` / full `LOSS` → cleared

**UI** (`ui/components/LiveSessionPill.kt`): Outer `AnimatedVisibility` with fade + half-screen slide; rounded 100-dp pill, Teal bg + white text when playing, `TealSoft` bg + `Muted` text when paused; inline icon + Korean label + 1 Hz ticker formatting `MM:SS`.

**Wiring.** `MainActivity.RootScaffold` now collects `audio.liveSession` and stacks `LiveSessionPill` above `NavigationBar`.

_Note: replaced by global `MiniPlayer` on 2026-05-14 (later same day)._

---

## ✓ 기록 tab split — Overview & History | TFI Scores — 2026-05-14

Structural refactor of the 기록 screen into two tabs via Compose `TabRow` + `HorizontalPager`. Source: [[wiki/sources/gemini-records-architecture-2026-05]].

**Layout.** `RecordsScreen` now hosts a `TabRow` above a `HorizontalPager` that swipes between two `LazyColumn`-backed pages.

**Tab 1 — 개요 및 기록.** Existing `CalendarCard` (unchanged), then `WeeklySummaryPlaceholder`, then a new "최근 기록" section. Single white card whose rows merge `ListeningSession` + `DiaryEntry` newest-first, capped at 50.

**Tab 2 — TFI 점수.** `TfiTrendPlaceholder`, then an "이전 검사" section listing each `TFIAssessment` newest-first. Full-width Teal `새 검사 시작` CTA at the bottom.

**Data layer.** `RecordsViewModel` constructor now takes `DiaryRepository` + `TfiRepository` alongside the existing `ListeningSessionRepository`. New flows: `recentEntries`, `tfiAssessments`.

Files: `ui/records/RecordsViewModel.kt`, `ui/records/RecordsScreen.kt`, `MainActivity.kt`.

**Verified 2026-05-14 on emulator-5554**: Build green; both tabs render; pager swipe works; CTA routes to TFI questionnaire.

---

## ✓ Home TFI prompt — wire-up + cadence gating — 2026-05-14

Two paired follow-ups to the TFI questionnaire. `TfiPromptCard`'s "시작 →" button was a `// TODO`; now wires through a new `onStartTfi` callback. `MainActivity` hands it `{ tfiVm.reset(); current = Tab.Settings; settingsSub = SettingsSub.Tfi }` — reuses the existing TFI rendering inside `Tab.Settings`.

Cadence gating: `HomeScreen` now takes `showTfiPrompt: Boolean`; the card is only rendered when due. `MainActivity` computes `System.currentTimeMillis() >= lastTfiDate + tfiCadenceWeeks × 7 × 24 × 60 × 60 × 1000`.

Files: `ui/home/HomeScreen.kt`, `MainActivity.kt`.

---

## ✓ ListeningSession minimum-duration floor 5 s → 3 min — 2026-05-14

`ListeningSessionRepository.MIN_DURATION_MS` raised from 5 000 to 180 000. Heatmap was over-counting low-signal short plays; 3 min is the deliberate compromise against Gemini's proposed 5-min floor (which would silently drop legitimate short TRT exposures).

---

## ✓ 기록 (Records) calendar — heatmap month grid — 2026-05-13

`IMG_2486.PNG` mock-driven redesign of the 기록 tab. Replaces the "준비 중" stub with a real month-grid heatmap calendar where day-cell intensity encodes daily listening duration. Re-toned to Teal (mock was coral); today indicator is a 2 dp Teal outline ring.

**Data layer — new `ListeningSession`.** Room v2 → v3 additive migration. Entity (`id, startedAtEpochMs, endedAtEpochMs, durationMs, presetLabel?`), `ListeningSessionDao`, `ListeningSessionRepository` (wraps DAO, exposes `logSession()` with a 5-second minimum-duration filter).

**AudioRepository session logging.** Constructor now takes `ListeningSessionRepository` + holds a private IO scope. `start()` records `currentSessionStartedAtMs`. `stop()` and the `AUDIOFOCUS_LOSS` branch finalise the session. `LOSS_TRANSIENT` does NOT split a session.

**`RecordsViewModel`.** Owns visible `YearMonth` (mutable via `previousMonth() / nextMonth()`). For each month, builds the 6×7 (42-cell) grid starting Sunday, bucket-sums `ListeningSession.durationMs` per `LocalDate`, computes `monthTotalMs` + `maxDayMs` over in-current-month cells only.

**`RecordsScreen`.** Cream `Bg` background, "기록" page header, then a single white card with a 6×7 day grid. Today gets a 2 dp `Teal` border; high-intensity cells render the date in white. Legend row: "적음 ▢▢▢▢ 많음" + "이번 달: Xh Ym".

**Heatmap ramp.** Added 5 tokens to `Color.kt`: `Heat0` through `Heat4`. Bucketing rule normalises by in-month max.

**Wiring.** `MainActivity.kt` instantiates `ListeningSessionRepository(db.listeningSessionDao())`, threads it into the `AudioRepository(...)` constructor.

**Verified 2026-05-13 on emulator-5554**: Migration ran cleanly; session logging works (5 s filter functional); calendar renders today's cell with Teal-4 fill.

---

## ✓ TFI questionnaire — 2026-05-12

25-item Tinnitus Functional Index — primary outcome measure, MCID = 13 points. Subscale-grouped pagination (8 pages, one per clinical subscale) + scoring + persistence + Settings entry points.

**Data layer.** New `TFIAssessment` entity (`id, takenAtEpochMs, items, totalScore, subscaleScores`) stored as JSON columns via a `MapTypeConverters` helper. Room v1 → v2 additive migration. New `TfiAssessmentDao` and `TfiRepository` that wraps the DAO + bumps `last_tfi_date` on submit. New DataStore keys `TFI_CADENCE_WEEKS` (default 2) and `LAST_TFI_DATE` (default 0).

**Scoring.** `ui/assessment/TfiScoring.kt` — pure-Kotlin helper. 8 subscales (I, SC, C, SL, A, R, Q, E), Korean labels. Total = mean of valid items × 10 (rounded, clamped 0–100). Meikle 2012 severity bands surfaced (0–17 경미, 18–31 경도, 32–53 중등도, 54–72 심함, 73–100 매우 심함).

**Content.** `ui/assessment/TfiContent.kt` — 25 items with Korean prompts + per-item anchor labels. Items present 0–10 uniformly across all items.

**UI.**
- `TfiQuestionnaireScreen.kt` — 8-page `HorizontalPager`. Sticky top: `← 닫기` + `n / 8` + thin teal progress bar.
- `TfiResultsScreen.kt` — `결과` eyebrow + `TFI 점수` headline. Total card + 세부 점수 card with 8 subscale rows.
- `TfiQuestionnaireViewModel.kt` — holds `responses: SnapshotStateMap<Int,Int>`; `submit()` calls the scoring helper via the repo.

**Wiring.** `MainActivity.kt` instantiates `TfiRepository`; new `enum class SettingsSub { Tfi, TfiResults }`; routing inside `Tab.Settings` switches between root / Tfi / TfiResults. `SettingsScreen.kt` gains new params for TFI cadence + entry.

**Verified 2026-05-12 on emulator-5554**: Migration ran cleanly; walked 8 pages; submitted total 51; subscale bars rendered correctly; DataStore + Room persistence verified.

---

## ✓ Tactile polish pass — press effects + polished play button — 2026-05-12

Reusable touch-feedback primitives in a new `ui/theme/PressEffect.kt`, then every tappable surface across the 5 redesigned screens converted to use them.

New primitives:
- `Modifier.pressableClickable { … }` — drop-in replacement for `.clickable`. Adds a snappy press-scale (default 0.96) via `graphicsLayer` + a `MutableInteractionSource` listener; spring `dampingRatio=0.62 / stiffness=820`.
- `PolishedPlayButton` — hero CTA play button. Layered render, 0.93 scale + shadow tuck on press, 76 dp default.
- `PlayButtonVariant` — `Coral` and `Orange`.

Conversions: HomeScreen, FrequencyMatchingScreen, SoundSettingsScreen, OnboardingScreen, SettingsScreen.

Files: new `ui/theme/PressEffect.kt`. Modified: `ui/home/HomeScreen.kt`, `ui/matcher/FrequencyMatchingScreen.kt`, `ui/sounds/SoundSettingsScreen.kt`, `ui/onboarding/OnboardingScreen.kt`, `ui/settings/SettingsScreen.kt`.

---

## ✓ Visual system migration + 홈 (Home) Direction A redesign — 2026-05-12

Theme migration applied across the chrome: status bar switched to light appearance (dark icons on cream), `NavigationBar` swapped to white surface + teal active + `TealSoft` indicator + muted inactive, `OrangeAccent` dropped from all screens. Matcher subscreen kept dark.

홈 rebuilt per wireframes Direction A + D: greeting + 치료 N주차 + settings gear; hero player card; week strip; sleep-timer chip; TFI prompt card.

Files: `MainActivity.kt`, `ui/theme/Theme.kt`, `ui/home/HomeScreen.kt`, `ui/records/RecordsScreen.kt`, `ui/settings/SettingsScreen.kt`. Commit `7fcbfa2`.

---

## ✓ 소리 (Sound) page redesign — 2026-05-12

Replaced the 4-card stub with wireframes Direction A (Calm settings list): header row, preset switcher, frequency card with `다시 측정` pill, processing-mode segmented control, color-noise pill row, ambient-mix progress bars. All non-frequency values display-only with `TODO` markers. Commit `5104bd7`.

---

## ✓ Onboarding Direction A redesign — 2026-05-12

Re-skinned the 7-page onboarding from dark+orange to Direction A (cream + teal + coral). Hero height 320 dp; section-specific Canvas art replaces the flat color block:
- Pages 1–3 — waveform pattern.
- Pages 4–6 — brain-node network.
- Page 7 — checkmark arc + progress dots.

File: `ui/onboarding/OnboardingScreen.kt`. Commit `3a15c1e`.

---

## ✓ 4-tab nav shell — 2026-05-11

`MainActivity.Tab` grew from `{ Matcher, Settings }` → `{ Home, Sound, Records, Settings }` per [[wiki/app/navigation]]. `FrequencyMatchingScreen` is no longer a tab — it's reached only from inside 소리 via a card. Default landing = 홈. Subscreen state inside 소리 is a single-level `mutableStateOf<SoundSub?>` + `BackHandler`.

New stubs: `ui/home/HomeScreen.kt`, `ui/sounds/SoundSettingsScreen.kt`, `ui/records/RecordsScreen.kt`. Nav icons: `Home / GraphicEq / CalendarMonth / Settings`.

**Verified 2026-05-11 on emulator-5554**: Clean install → onboarding → 홈 default; all 4 tabs render; CTA routes between tabs; matcher subscreen + back behavior correct.

---

## ✓ Onboarding tutorial — 2026-05-11

7-page swipeable onboarding (HorizontalPager) gating the app on first launch. Korean copy synthesized from `wiki/clinical/*` — each page has a `// Source: wiki/clinical/<file>.md` comment for traceability.

Page layout:

| # | Section | Title | Source |
|---|---|---|---|
| 1 | 1. 이명에 대해 | 이명은 무엇인가요? | `clinical/tinnitus.md` |
| 2 | 1. 이명에 대해 | 왜 들리는 걸까요? | `clinical/central-gain.md` |
| 3 | 1. 이명에 대해 | 치료의 목표는 "습관화" | `clinical/habituation.md` + `clinical/triple-network-model.md` |
| 4 | 2. 치료 접근 | TRT — 12–24개월의 여정 | `clinical/trt.md` |
| 5 | 2. 치료 접근 | 사운드 치료: 마스킹이 아닙니다 | `clinical/sound-therapy.md` |
| 6 | 2. 치료 접근 | 인지행동치료(CBT)와 보조 요법 | `clinical/cbt.md` |
| 7 | 3. 앱 사용법 | 이 앱 사용법 | implemented Android surfaces |

Files: new `ui/onboarding/OnboardingScreen.kt`, `ui/onboarding/OnboardingViewModel.kt`. Modified: `data/repository/UserSettingsRepository.kt`, `MainActivity.kt`, `ui/settings/SettingsScreen.kt`.

**Verified 2026-05-11 on emulator-5554**: Full clean install + replay flows + Skip flow all verified; zero `AndroidRuntime` FATAL.

---

## ✓ Audio focus & interruption handling — 2026-05-11

New `audio/routing/AudioFocusController.kt` — single `AudioFocusRequest` (USAGE_MEDIA + CONTENT_TYPE_MUSIC; `setWillPauseWhenDucked(true)`). Listener mirrors LOSS / GAIN into its own `hasFocus` flag.

Interruption policy (in `AudioRepository.onFocusChange`):
- `LOSS` → full teardown
- `LOSS_TRANSIENT` / `LOSS_TRANSIENT_CAN_DUCK` → engine off, **service stays in foreground**, latch set.
- `GAIN` after a transient → engine restarts automatically.

Files: new `audio/routing/AudioFocusController.kt`. Modified: `data/repository/AudioRepository.kt`.

**Verified 2026-05-11 on emulator-5554** with simulated GSM call: transient pause + auto-resume + user-stop + re-acquire all clean.

---

## ✓ Implement `TherapyAudioService` (foreground service) — 2026-05-11

Pattern chosen: **Option A** — `AudioEngine` is a process-singleton on `TinnitusTrackerApp : Application`; foreground service is started-only (no binding); `AudioRepository` wraps everything. Rationale in `/Users/midnight/.claude/plans/luminous-baking-rocket.md`.

Files: new `TinnitusTrackerApp.kt`, `audio/service/TherapyAudioService.kt`, `audio/service/AudioNotification.kt`, `data/repository/AudioRepository.kt`, `res/drawable/ic_notification_wave.xml`. Modified: `AndroidManifest.xml`, `MainActivity.kt`, `FrequencyMatchingViewModel.kt` + factory.

Notification design: single ongoing low-importance notification, channel `therapy_audio`, NOTIF_ID 1001, one "정지" action.

**Verified 2026-05-11 on emulator-5554**: `dumpsys` confirms `isForeground=true types=0x00000002 (mediaPlayback)`; survives HOME backgrounding; clean teardown on 정지.

---

## ✓ Wire up Room database — 2026-05-11

Room 2.6.1 + KSP wired in `app/build.gradle.kts`. `AppDatabase` singleton v1 (`exportSchema = true`). First entity `DiaryEntry` + `DiaryDao`. `DiaryRepository` thin wrapper, manually injected in `MainActivity`. Non-residual round-trip sanity check runs on every cold start under tag `RoomScaffold`. **Verified 2026-05-11 on emulator-5554**.

---

## ✓ Build & extend the tinnitus frequency selecting page — base 2026-05-08, extensions 2026-05-11

**Base (2026-05-08):** `FrequencyMatchingScreen.kt` + `FrequencyMatchingViewModel.kt`. Logarithmic dial (100 Hz – 16 kHz), volume hard-cap at 70 %, octave-confusion check (auto-scrolls into view after 2 s of inactivity), DataStore persistence, "can't find pitch" skip affordance. Stepper buttons ±10 / ±100 Hz; all inputs snap to nearest 10 Hz.

**Extensions (2026-05-11)** — per `raw/Plan(수기).md` §3:
- **Notch / amplify mode selector** — new `PROCESSING_MODE` string preference; `ProcessingModeCard` shown via `AnimatedVisibility` when `hasTonalTinnitus == true`.
- **Seamless matching → therapy handoff** — `confirmPitch()` switches the engine to the saved processing mode mid-stream; `BiquadFilter.reset()` runs on topology change so there is no audible click.
