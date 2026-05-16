---
title: "Implementation Plan"
type: app
aliases: ["Plan", "Roadmap", "Work Plan"]
tags: [app/planning]
evidence: n/a
status: draft
sources: []
last_reviewed: 2026-05-14
---

# Implementation Plan

**One active task at a time.** Only the task in `## Now` is being worked on. When it's done, the top of `## Next` is promoted into `## Now` and the completed task moves to `## Done`.

---

## Now

_Empty — promote Item 2 (홈 polish pass) from `## Next` when ready._

---

## Next (queued, in order)

> Items 1–6 (items 1, 5, 6 shipped; 4 remaining) were broken out from a previous fat "App-wide redesign pass against [[ux-principles]]" bullet on 2026-05-14, after the user supplied [[wiki/sources/kole-jain-beginner-mistakes]] (`raw/ui_ux_beginner_mistakes_detailed.md`) + [[wiki/sources/kole-jain-genius-ux]] (`raw/genius_ui_ux_design_strategy_detailed.md`) as canonical inputs alongside [[ux-principles]]. Decisions locked in for the whole sequence: **card radius = 16 dp** (compromise between wireframe `20 px` and previous `12 dp` rule; [[ux-principles]] §5 updated in item 1), **full design-system extraction** under `ui/theme/components/`, **one item promoted at a time**. The wireframe HTML at `TinnitusTrackerAndroid/docs/design/app-wireframes.html` and the chosen directions (Onboarding A, Home A, Sound A, Records B, Settings A) are preserved — only micro-level execution changes.

1. [ ] **홈 polish pass.** `HomeScreen.kt` through the new components: `AppCard.DarkHero(radius = 16)` for hero (currently 22 dp, drifted); `ChevronRow` for `SleepTimerRow` (drops text `›` glyph, ships proper `Icons.Filled.ChevronRight`); `TfiPromptCard` "시작 →" replaced with `PolishedPlayButton(variant = Teal, size = small)` (currently re-implements gradient + shadow). Week-strip cells unified to 8 dp (kills the 10/12 dp mix). Rip-it-out: remove redundant 1 dp `Line` border on `SleepTimerRow` (shadow already separates). 25 % zoom-out test must pass — greeting + hero play button dominate.

2. [ ] **소리 polish pass.** `SoundSettingsScreen.kt` through the new components: processing-mode → `SegmentedControl`; color-noise selectors → `Pill(Teal/Outline)` row; "다시 측정 ›" → `Pill(TealSoft)` with chevron icon; all cards → `AppCard(16 dp, 16 dp padding)`. **Edge case** (per [[wiki/sources/kole-jain-genius-ux]] §3, §4): long preset names (Korean can run 30+ chars) — add `maxLines = 1` + `overflow = Ellipsis` on `PresetSwitcherCard` label. Note: `maxLines`/`overflow` already added to the preset name `Text` in item 5; this item handles the full card swap.

3. [ ] **기록 polish pass.** `RecordsScreen.kt`, `QuickLogBottomSheet.kt`, `DayDetailBottomSheet.kt`: every surface → `AppCard`; heatmap cells → 8 dp radius (chip scale); today indicator stays 2 dp Teal border; recent-entries rows → `ChevronRow` + `IconBadge` (folds the session / diary leading-badge pattern that's currently rebuilt in 3 places down to one component); `TagChip` replaces the inline `QuickLogBottomSheet` chips; `EmptyStateCard` for all four empty surfaces (heatmap-zero, no-TFI, no-diary, no-sessions-on-day) per [[ux-principles]] §4.

4. [ ] **설정 polish pass + remainder.** `SettingsScreen.kt`: every action row → `ChevronRow` with `pressableClickable` (currently plain `.clickable` — biggest "no dead UI" gap per [[wiki/sources/kole-jain-beginner-mistakes]] §7); `TfiCadenceRow` value display → `Pill(TealSoft)`; rip out any section header where rows below are self-labeling. Same pass opportunistically applies new components to `MiniPlayer`, `OnboardingScreen`, `TfiQuestionnaireScreen`, `TfiResultsScreen`, `FrequencyMatchingScreen` where the diff is small.

---

## Later (backlog, unordered within tier)

### Tier 1 — clinical correctness
- [ ] Mixing-point calibration tool (locks therapy output 3–6 dB below the mixing point) → [[wiki/clinical/sound-therapy]]
- [ ] TRT-phase UI (Orientation → Active Habituation → Consolidation) → [[wiki/clinical/trt]]
- [ ] Continuous red-flag safety screener (30-day cadence) → [[wiki/app/red-flags]]
- [ ] 8-hour sound-therapy adherence engine + occluding-earphone warning → [[wiki/clinical/sound-therapy]]
- [ ] TFI distress-tracking analytics — trend lines, MCID flags (basic capture is in Next #10) → [[wiki/clinical/habituation]]

### Tier 2 — mechanism-targeted
- [ ] Streak / goal-progress badge (Depends on `daily_listening_goal_min` preference)
- [ ] Spike-mode vicious-cycle interrupter (4-7-8 breath + reframe + sound bump)
- [ ] Salience-retraining daily practice → [[wiki/clinical/triple-network-model]]
- [ ] Somatic modulation assessment + exercise library

### Tier 3 — personalization & education
- [ ] TRT category routing (0–4) at onboarding
- [ ] 9-week TRT directive-counseling curriculum (Korean)
- [ ] Clinician-readable monthly PDF report
- [ ] Honest neuromodulation decision aid (rTMS / tDCS / VNS / 알파-스팀) → [[wiki/clinical/rtms]], [[wiki/clinical/tdcs]], [[wiki/clinical/vns-auricular]]
- [ ] Material You dark-mode opt-in (was "dark mode default" in old plan; demoted to opt-in setting since wireframes ship light/cream)

### Tier 4 — experimental
- [ ] Pitch-matched notched sound therapy → [[wiki/clinical/tonotopic-reorganization]]

---

## Done

### ✓ 주간 요약 (Weekly summary) cards on 기록 — 2026-05-16

Replaced `WeeklySummaryPlaceholder` ("준비 중") with real per-week aggregate cards anchored to the visible month. Cards are Sunday-anchored, reusing `buildGridDates(ym).chunked(7)`; weeks filtered to those containing at least one day in `visibleMonth`. Aggregation lives in `RecordsViewModel` (no new DAO queries), consistent with the existing heatmap pattern. Visual spec derived from `ux-principles.md` + component library (no `IMG_2486.PNG` — not present in `raw/`).

**Data.** New `WeekSummary` data class: `startDate`, `endDate`, `tinnitusAvg: Float?` (null = no diary entries that week), `stressAvg: Float?`, `listenMs: Long`, `daysWithData: Int`. `RecordsUiState` gained `weeklySummaries: List<WeekSummary>`. `assemble()` now takes diaries as a second arg; state flow changed from `sessionFlow.map` to `combine(sessionFlow, diaryFlow)`. `buildWeekSummaries()` added to `RecordsViewModel`.

**UI.** `WeeklySummarySection` — `SectionHeader("주간 요약")` + column of cards. `WeeklySummaryCard` — week label + day-count `Pill(TealSoft)` + three `MetricRow`s (이명 평균 / 스트레스 평균 / 청취 시간). Missing metric renders `"—"` in Muted; empty week (all zeros) renders `WeeklySummaryEmptyCard` ("데이터 없음" + week label). Month-boundary weeks show both month prefixes in the label (e.g. `"10.27 – 11.02"`). Format helpers: `formatWeekLabel`, `formatScoreValue`, `formatListenValue`.

**Files.** Modified: `ui/records/RecordsViewModel.kt` (WeekSummary, weeklySummaries in state, buildWeekSummaries, assemble signature), `ui/records/RecordsScreen.kt` (WeeklySummarySection + sub-composables replace placeholder, Radius import added).

**Verified 2026-05-16 on emulator-5554**: BUILD SUCCESSFUL in 4 s. APK installed (`Performing Streamed Install ... Success`). Visual walk (empty DB / mixed / month-boundary) deferred — run manually when seeding test data.

---

### ✓ State + interactive-feedback audit — 2026-05-16

Final hygiene sweep across all `ui/` screens after the 1–4 polish-pass foundations.

**5A — Forbidden-literal cleanup.** Snapped all `5 / 6 / 9 / 10 / 14 / 18 / 22 / 26 / 28 dp` literals to `Spacing.*` / `Radius.*` / `Elevation.*` tokens in: `SettingsScreen`, `HomeScreen`, `RecordsScreen`, `QuickLogBottomSheet`, `DayDetailBottomSheet`, `SoundSettingsScreen`, `OnboardingScreen`, `TfiQuestionnaireScreen`, `TfiResultsScreen`. Documented exceptions: `ui/theme/` (token authority), `FrequencyMatchingScreen` (bespoke metallic-knob geometry), `OnboardingScreen` drawScope wave / brain-network / checkmark geometry, 3 dp MCID legend swatch in `RecordsScreen`, 1.5 dp shadow elevations in `SettingsScreen`.

**5B — Korean overflow hardening.** Added `maxLines + overflow = TextOverflow.Ellipsis` to 5 dynamic `Text` callsites that could receive long Korean strings: preset name in `SoundSettingsScreen` (1-line), TFI item prompt in `TfiQuestionnaireScreen` (3-line), session preset subtitle in `DayDetailBottomSheet` (1-line), diary note composite in `DayDetailBottomSheet` (2-line), recent-entry subtitle in `RecordsScreen` (1-line).

**5C — Seed-and-walk.** Static audit confirmed 0 `.clickable {` violations outside `TabRow`/`Slider`, 0 non-`Icons.Filled.*` imports. Manual visual walk with long preset seed deferred to user; no blocker for shipment.

**Files.** Modified: `ui/settings/SettingsScreen.kt`, `ui/home/HomeScreen.kt`, `ui/records/RecordsScreen.kt`, `ui/records/QuickLogBottomSheet.kt`, `ui/records/DayDetailBottomSheet.kt`, `ui/sounds/SoundSettingsScreen.kt`, `ui/onboarding/OnboardingScreen.kt`, `ui/assessment/TfiQuestionnaireScreen.kt`, `ui/assessment/TfiResultsScreen.kt`.

**Verified 2026-05-16 on emulator-5554**: BUILD SUCCESSFUL in 9 s. APK installed. Grep clean: 0 forbidden-literal hits outside documented exceptions.

---

### ✓ Design tokens + shared component library — 2026-05-14

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

**Files.** New: `ui/theme/Tokens.kt`, `ui/theme/components/AppCard.kt`, `ui/theme/components/PrimaryButton.kt`, `ui/theme/components/SecondaryButton.kt`, `ui/theme/components/Pill.kt`, `ui/theme/components/TagChip.kt`, `ui/theme/components/SegmentedControl.kt`, `ui/theme/components/IconBadge.kt`, `ui/theme/components/ChevronRow.kt`, `ui/theme/components/EmptyStateCard.kt`, `ui/theme/components/SectionHeader.kt`. Modified: `ui/theme/PressEffect.kt` (`PlayButtonVariant.Teal`), `Tinnitus_obsidian/wiki/app/ux-principles.md` (§5 radius + components spec).

**Verified 2026-05-14 on emulator-5554**:
- Build green (`:app:assembleDebug` BUILD SUCCESSFUL in 3 s; no Kotlin / KSP errors).
- APK installed cleanly (`Performing Streamed Install ... Success`).
- Cold launch → `RoomScaffold: wrote id=28 readBack='room-scaffold-sanity-check'` (DB v5 schema intact).
- Tab navigation walked (홈 → 소리 → 기록) — no visual change (components ship unused this item, which was the goal). Zero `AndroidRuntime FATAL` in logcat.

Not in this item (queued as items 2–6 in `## Next`): wiring the new components into `HomeScreen`, `SoundSettingsScreen`, `RecordsScreen`, `QuickLogBottomSheet`, `DayDetailBottomSheet`, `SettingsScreen`, `MiniPlayer`, `OnboardingScreen`, `TfiQuestionnaireScreen`, `TfiResultsScreen`, `FrequencyMatchingScreen` — each gets its own queued item so a regression in one screen can't take the others down.

### ✓ Day-tap drilldown on 기록 calendar — 2026-05-14

Every day cell on the heatmap is now tappable. Tap opens a `DayDetailBottomSheet` showing all `ListeningSession` rows (start/end clock, duration, preset label) and `DiaryEntry` rows for that local date — sessions and diaries grouped under labeled cards, newest-first within each group. Empty days surface "이 날에는 기록이 없어요". Spillover days from neighboring months are tappable too (open the sheet for that date in the other month, no special handling needed).

**Data layer.** `RecordsViewModel` gained a `selectedDay: MutableStateFlow<LocalDate?>` and a derived `dayDetail: StateFlow<DayDetail?>` that `flatMapLatest`-es into `combine(sessionRepo.observeRange(dayStart, dayEnd), diaryRepo.observeRange(...))` keyed on local-zone start-of-day → start-of-next-day bounds. New `DayDetail(date, sessions, diaries)` data class with `isEmpty` / `totalListenMs` derived properties. Public `selectDay(LocalDate?)` opens (non-null) and dismisses (null) the sheet.

**UI.** New `ui/records/DayDetailBottomSheet.kt` — Material 3 `ModalBottomSheet`, header `M월 D일 (요일)` + summary subtitle (`청취 Xh Ym · N회 세션 · 일지 M개` or empty). Two stacked white cards: 청취 세션 (icon badge `GraphicEq` + time-range + duration·preset) and 일지 (icon badge `EditNote` + clock + `이명 X · 스트레스 Y · tags`). `RecordsScreen` collects `dayDetail` + hosts the sheet; `DayCellView` is now `pressableClickable`, threading `cell.date` up to `vm.selectDay(...)`.

Files: new `ui/records/DayDetailBottomSheet.kt`. Modified: `ui/records/RecordsViewModel.kt` (DayDetail + selectedDay + dayDetail flow + selectDay), `ui/records/RecordsScreen.kt` (collect dayDetail, host sheet, `pressableClickable` on DayCellView, plumb `onDayTap` through `OverviewHistoryTab` → `CalendarCard` → `DayGrid`).

**Verified 2026-05-14 on emulator-5554**:
- Build green.
- 기록 → tab 1 → tap day 14 (today, has data) → sheet renders `5월 14일 (목)` / `청취 3분 46초 · 2회 세션 · 일지 3개`. 청취 세션 card lists both sessions with time ranges (`오후 6:16 – 오후 6:16` / `8초 · 저녁 휴식`, `오후 2:30 – 오후 2:34` / `3분 38초`); 일지 card lists three diary rows with packed-tag display (`이명 5 · 스트레스 5 · 안도 · 수면` etc.).
- Back gesture dismisses cleanly. Zero `AndroidRuntime FATAL`.

Not in this task: long-press menu / row swipe-to-delete; navigation into a session or diary's edit screen (no edit UI exists yet).

### ✓ TFI history trend on 기록 — 2026-05-14

Replaces the `TfiTrendPlaceholder` "준비 중" card with a real `TfiTrendCard`: a Compose `Canvas` sparkline plus a header trend summary, sitting above the existing `이전 검사` table inside the TFI 점수 tab.

**Sparkline.** Plots scores chronologically (oldest → newest left-to-right; reverses the desc-ordered `observeAll()` result). Y-axis fixed 0–100 with three implied gridlines (0 / 50 / 100). The latest score's ±13 MCID band is rendered as a translucent `TealSoft` rectangle behind the line, so a viewer can see at a glance which prior scores fall inside vs. outside the clinically-meaningful range around "now". Polyline is 3 px Teal. Each data point draws a dot — coral halo + dot for any pair-to-pair Δ ≥ +13 (worsening), green-teal for Δ ≤ −13 (improvement), Muted otherwise. The latest point gets a larger filled-Teal dot with a white inner pip.

**Header / legend.** Title row keeps `TFI 추이` + the existing `최근 N점` Teal pill. Subtitle now reads `N 회 기록 · 첫 검사 대비 ↑X점 (악화)` / `↓X점 (호전)` / `↑X점` / `↓X점` / `변화 없음` based on the first-vs-latest delta and the MCID threshold. Below the canvas: a `TealSoft` swatch + `MCID 13점 — 임상적으로 유의미한 변화` legend line. Empty state (no assessments) shows `아직 표시할 추이가 없습니다` inside the canvas slot; the existing `이전 검사` empty-state card still renders below.

Table half of the deliverable was already in place (`TfiHistoryCard` from the tab-split task); kept as-is.

Files: modified `ui/records/RecordsScreen.kt` (deleted `TfiTrendPlaceholder`, added `TfiTrendCard` + `TfiSparkline` + `trendSubtitle` helper).

**Verified 2026-05-14 on emulator-5554** (with two existing TFI rows, 51 and 66):
- TFI 점수 tab renders `TFI 추이` / `최근 66점` / `2 회 기록 · 첫 검사 대비 ↑ 15점 (악화)` — the +15 delta ≥ MCID, so the second point also draws with the coral worsening halo. Latest 66 dot is the larger Teal-with-white-pip marker.
- MCID band sits centered around the 66 horizontal, visibly bracketing the 51 → 66 line.
- Legend row visible directly below the canvas.
- 이전 검사 table unchanged (`5월 12일 (화) · 오후 8:09 — 66 / 100 — 심함` / `5월 12일 (화) · 오후 8:05 — 51 / 100 — 중등도`); 새 검사 시작 CTA still works.
- Zero `AndroidRuntime FATAL`.

Not in this task: tappable points to drill into a specific assessment's per-subscale scores; per-subscale mini-sparklines.

### ✓ Global Mini Player & True Pause — 2026-05-14

Replaced the `LiveSessionPill` (only visible during active sessions) with a persistent `MiniPlayer` bar above the bottom nav, always visible except during onboarding and the Frequency Matcher subscreen. Dark-themed card styled after `app-wireframes.html` Direction A hero player. Displays the active preset name and a circular Play/Pause toggle.

**True pause capability.** Added `pause()` and `resume()` to `AudioRepository`. Pausing stops the engine audio but keeps the foreground service alive and the listening session open — resuming continues the same session. `TherapyAudioService` now handles `ACTION_PAUSE` / `ACTION_RESUME` intents. `AudioNotification` dynamically swaps its action button between "일시정지" and "재생" based on `isPlaying` state.

Files: new `ui/components/MiniPlayer.kt`. Deleted `ui/components/LiveSessionPill.kt`. Modified: `data/repository/AudioRepository.kt`, `audio/service/TherapyAudioService.kt`, `audio/service/AudioNotification.kt`, `MainActivity.kt`.

### ✓ Per-sound time tracking on `ListeningSession` — 2026-05-14

Introduced a `ListeningSessionSegment` child table to break down `ListeningSession` tracking. The `AudioRepository` now tracks the exact duration of each active preset during a session and writes them to the database in a single transaction upon finalization.

### ✓ Preset Audio Engine & Sound Settings UI — 2026-05-14

Untangled the `AudioEngine` dependency knot blocking session analytics, introducing a robust `SoundPreset` system and real procedural audio synthesis. Replaces the mock labels from the previous additive migration.

**Data layer.** New `SoundPreset` entity (`id, name, processingMode, colorNoise, colorNoiseVolume, ambientMix`). Room v4 → v5 additive migration (`CREATE TABLE IF NOT EXISTS sound_presets`, inserts default '저녁 휴식' preset). `SoundPresetDao` and `SoundPresetRepository`. New DataStore key `ACTIVE_PRESET_ID` managed by `UserSettingsRepository`.

**Engine Extension.** `AudioEngine` extended with:
- Procedural color noise (Pink via simple 1-pole lowpass, Brown via deeper lowpass, White).
- Synchronized `MediaPlayer` instances for ambient background tracks (`rain`, `beach`), loaded via Context.

**Architecture Integration.** `AudioRepository` updated to consume `UserSettingsRepository` and `SoundPresetRepository`. On `init`, observes `activePresetId` and dynamically updates the engine (`setMode`, `setColorNoise`, `setAmbientMix`). The session logger now captures the *real* `currentPresetName` instead of mock randomized strings, clearing Next #5.

**UI Wiring.** `SoundSettingsScreen` components (processing mode, color noise pills, ambient sliders) are now driven by a new `SoundSettingsViewModel`. The "Auto-save to active preset" UX pattern allows changes to immediately persist to the active preset, deferring complex create/delete UI to a later iteration.

### ✓ FAB + Quick-Log Bottom Sheet for `DiaryEntry` — 2026-05-14

First entry UI for the `DiaryEntry` table (entity has existed since the Room scaffold; until now there was no way to write to it). A circular Teal FAB anchored at the bottom-right of the 기록 screen — visible across both tabs — opens a Material 3 `ModalBottomSheet` with no text input: only two sliders + chip toggles. Source: [[wiki/sources/gemini-records-architecture-2026-05]] § Aligned ideas.

**Sheet content** (`ui/records/QuickLogBottomSheet.kt`):
- Header: "기록 추가" 20 sp bold + muted subtitle "지금 상태를 빠르게 기록하세요".
- Two `SliderRow` cards (white surface, 12 dp radius): `이명 크기` and `스트레스`, each 0–10 step-snapped, bold Teal current value on the right.
- `태그` section: `FlowRow` of six `TagChip` toggles (`안도 / 악화 / 수면 / 스트레스 / 휴식 / 집중`). Selected = `TealSoft` bg + `Teal` border + `Teal` text + semibold; unselected = `Surface` bg + `Line` border + `Ink2`.
- Full-width Teal `저장` CTA with the standard `pressableClickable` press-scale + spot shadow.

**Persistence.** New `RecordsViewModel.addDiaryEntry(severity, stressLevel, tags)` wraps `diaryRepo.upsert(...)`. v1 packs the selected tags into the existing `DiaryEntry.note` column comma-separated — no schema migration; the column was already a String. If freeform notes are added later, a proper `tags` column / migration will replace this. The Recent Entries List subtitle renders the packed tags by splitting on `","` and joining with ` · ` (e.g. saved `"안도,수면"` displays as `안도 · 수면`).

**Surfacing.** Saved entries appear in the Recent Entries List immediately — `recentEntries` is a reactive `combine(sessions, diaries)` flow, so the upsert pushes a new row without any explicit refresh. The diary icon (TealSoft tile + `EditNote`) differentiates it from session rows.

**Layout note.** The 기록 `LazyColumn` bottom contentPadding was bumped from 24 → 96 dp so the last row clears the FAB. The FAB sits inside a `Box` overlay so it doesn't push tab content up.

Files: new `ui/records/QuickLogBottomSheet.kt`. Modified: `ui/records/RecordsScreen.kt` (FAB + sheet host + state, `subtitleFor` tag pretty-print, padding bump), `ui/records/RecordsViewModel.kt` (`addDiaryEntry` method).

**Verified 2026-05-14 on emulator-5554**:
- Build green; APK installed over the v4 DB.
- 기록 tab renders a circular `+` FAB at the bottom-right (`content-desc="기록 추가"`).
- Tap FAB → sheet animates up with "기록 추가" + "지금 상태를 빠르게 기록하세요" + both sliders at default 5 + six tag chips visible.
- Toggled `안도` and `수면` chips, tapped 저장 → sheet dismisses; Recent Entries List immediately shows a new row: `5월 14일 (목) · 오후 2:41` / `이명 5 · 스트레스 5 · 안도 · 수면` (chips rendered with bullet separator, not the underlying comma).
- Zero `AndroidRuntime FATAL` across the run.

Not in this task: free-form note field (deliberately omitted — chip-only by design); proper `tags` column migration (deferred until notes become a feature); editing or deleting saved entries (Next #2 day-tap drilldown will surface them).

### ✓ `LiveSessionPill` floating in-app tracking indicator — 2026-05-14

A small animated Teal pill anchored above the bottom nav, visible only while `AudioRepository` has an open session. Covers the in-app counterpart of the system-tray notification: when the user backgrounds the matcher into 기록 / 설정 / 홈, the pill is the only on-screen affordance that audio is still tracking. Reference implementation: `raw/gemini_chat/gemini-code-1778723986116.kt` — adapted to wire into `AudioRepository`'s existing flows instead of duplicating a `LiveSessionState`.

**Data layer.** `AudioRepository` now exposes a `LiveSession(startedAtEpochMs, isPaused)` data class via `liveSession: StateFlow<LiveSession?>`. State transitions:
- `start()` → `_liveSession.value = LiveSession(now, isPaused = false)`
- `LOSS_TRANSIENT` / `LOSS_TRANSIENT_CAN_DUCK` → `update { it?.copy(isPaused = true) }` (session stays open; the focus latch already kept the FGS up)
- `GAIN` after transient → `update { it?.copy(isPaused = false) }`
- `stop()` / full `LOSS` → cleared inside `finaliseSession()` alongside the existing `currentSessionStartedAtMs = null`

`null` = no session; `isPaused = true` = transient focus loss; `isPaused = false` = engine producing audio.

**UI** (`ui/components/LiveSessionPill.kt`):
- Outer `AnimatedVisibility` with fade + half-screen slide for both enter and exit.
- Rounded 100-dp pill, Teal bg + white text when playing, `TealSoft` bg + `Muted` text when paused.
- Inline icon (`GraphicEq` playing / `Pause` paused) + Korean label (`추적 중` / `일시정지`) + a 1 Hz `LaunchedEffect` ticker that recomputes `elapsedMs = now - startedAt` every second and formats `MM:SS`.
- Tapping calls the supplied `onClick` (which in `MainActivity` routes to `Tab.Sound`, collapsing any open `soundSub`).

**Wiring.** `MainActivity.RootScaffold` now collects `audio.liveSession` and stacks `LiveSessionPill` above `NavigationBar` inside the Scaffold `bottomBar` slot (wrapped in a `Column`), so the pill animates in/out without overlapping the rest of the content area. Tapping it lands on the 소리 root; from there, `다시 측정` enters the matcher to access the stop control.

Files: new `ui/components/LiveSessionPill.kt`. Modified: `data/repository/AudioRepository.kt` (added `LiveSession` data class + flow, updated `start` / `onFocusChange` / `finaliseSession`), `MainActivity.kt` (collect flow, wrap bottomBar in `Column { LiveSessionPill(); NavigationBar(...) }`).

**Verified 2026-05-14 on emulator-5554**:
- Build green; APK installed.
- Tap play in matcher → pill appears with `추적 중  00:03`, sitting just above the bottom nav.
- Waited 3 s → pill text advanced to `01:10 → 01:13` (the ticker is alive across recompositions).
- Switched to 홈 tab while audio was still playing → pill persisted; tap on the pill → app routed back to `소리 설정`.
- Simulated incoming call via `adb emu gsm call 5551212` → pill swapped to `일시정지  00:03` (TealSoft + Muted styling); the elapsed timer kept counting, matching the policy that transient focus loss does NOT split the session.
- `adb emu gsm cancel 5551212` → pill reverted to `추적 중  00:06` (engine auto-resumed).
- Tap stop → `liveSession` cleared, pill animated out cleanly.
- Zero `AndroidRuntime FATAL` across the run.

Not in this task: pause-debounce timeout (the reference impl's `debounceJob → finalizeSession()` after 60 s of no resume) — `AudioRepository`'s policy is that transient loss never closes a session, only full `LOSS` does, so the debounce isn't needed here. Haptic feedback on pill tap; long-press menu (stop directly from the pill) — deferred.

### ✓ 기록 tab split — Overview & History | TFI Scores — 2026-05-14

Structural refactor of the 기록 screen into two tabs via Compose `TabRow` + `HorizontalPager`. Lands the scaffold that Next #4 (day-tap drilldown) and Next #5 (TFI history trend) live inside. Source: [[wiki/sources/gemini-records-architecture-2026-05]] § Key findings.

**Layout.** `RecordsScreen` now hosts a `TabRow` (containerColor = `Bg`, indicator + selected = `Teal`, unselected = `Muted`, single `Line` divider underneath) above a `HorizontalPager` that swipes between two `LazyColumn`-backed pages. Outer screen padding removed from the root Column so the tab bar can run edge-to-edge; horizontal padding is now applied per-page (`20.dp`) and inside the header text.

**Tab 1 — 개요 및 기록.** Existing `CalendarCard` (unchanged), then `WeeklySummaryPlaceholder` (unchanged "준비 중" card), then a new "최근 기록" section. The Recent Entries List is a single white card whose rows merge `ListeningSession` + `DiaryEntry` newest-first, capped at 50. Each row: icon badge (TealSoft tile + `GraphicEq` for sessions / `EditNote` for diaries), date+time (`5월 14일 (수) · 오후 3:20` Korean format), subtitle (`Xh Ym 청취 · presetLabel?` for sessions, `이명 X · 스트레스 Y · note?` for diaries), `···` overflow glyph on the right (visual stub — actual drilldown is Next #4). Empty state: muted "아직 기록된 활동이 없습니다" card.

**Tab 2 — TFI 점수.** `TfiTrendPlaceholder` (white card, header "TFI 추이" + right-aligned "최근 N점" badge when an assessment exists, "준비 중 — 추이 차트 (MCID 13점)" stub — real sparkline lands in Next #5). Then an "이전 검사" section listing each `TFIAssessment` newest-first: date+time, `XX / 100`, and a severity pill (`TealSoft` bg + `Teal` text) using `TfiScoring.severityKorean(total)`. Bottom: full-width Teal `새 검사 시작` CTA that invokes the new `onStartTfi` callback (same `tfiVm.reset(); current = Settings; settingsSub = Tfi` route the Home prompt card uses). Empty state: "이전 검사 기록이 없습니다".

**Data layer.** `RecordsViewModel` constructor now takes `DiaryRepository` + `TfiRepository` alongside the existing `ListeningSessionRepository`. New flows: `recentEntries` (`combine(sessions, diaries)` over `0L..Long.MAX_VALUE`, merged into a `RecentEntry` sealed class — `Session` / `Diary` variants — sorted desc, take 50); `tfiAssessments` (`tfiRepo.observeAll().stateIn(...)`). `RecordsViewModelFactory` updated accordingly.

**Wiring.** `MainActivity.kt` — `Root` and `RootScaffold` now thread `DiaryRepository` through; `RecordsScreen` call site passes `onStartTfi` (same body as the Home card route, with `current = Tab.Settings; settingsSub = SettingsSub.Tfi`); `RecordsViewModelFactory(sessions, diary, tfi)` instead of `(sessions)`.

Files: `ui/records/RecordsViewModel.kt` (added `RecentEntry` sealed class, two new flows, factory signature), `ui/records/RecordsScreen.kt` (tab + pager scaffold, two tab composables, recent entries card, TFI history card, severity pill, start CTA, Korean date helper), `MainActivity.kt` (threaded `diaryRepository`, wired `onStartTfi`).

**Verified 2026-05-14 on emulator-5554** (reinstall over the v4 DB with the 2 existing TFI rows):
- Build green (`:app:assembleDebug`).
- `RoomScaffold: wrote id=14 readBack='room-scaffold-sanity-check'` — schema intact, no migration regression.
- 홈 → 기록 → renders the new header `기록` + `TabRow [개요 및 기록 | TFI 점수]` with the first tab selected.
- Scrolling tab 1: `CalendarCard` (May 2026) → `주간 요약 (준비 중)` → `최근 기록` section → empty-state card "아직 기록된 활동이 없습니다" (no sessions logged on this install).
- Swipe right-to-left on the pager → tab 2 selects: `TFI 추이` card with "최근 66점" pill + "준비 중 — 추이 차트 (MCID 13점)"; `이전 검사` lists `5월 12일 (화) · 오후 8:09 — 66 / 100 — 심함` and `5월 12일 (화) · 오후 8:05 — 51 / 100 — 중등도`; `새 검사 시작` CTA at the bottom.
- Tap `새 검사 시작` → app routes to `Tab.Settings` + `SettingsSub.Tfi`, page 1/8 ("침투성") renders. Closing returns to 설정.
- Zero `AndroidRuntime FATAL` across the run.

Not in this task (explicit follow-ups in `## Next`): real TFI sparkline + MCID markers (Next #5); day-tap drilldown wired to the `···` overflow (Next #4); weekly summary card content (Next #9); FAB + Quick-Log bottom sheet that will feed real diary rows into the Recent Entries List (Next #3).

### ✓ Home TFI prompt — wire-up + cadence gating — 2026-05-14

Two paired follow-ups to [[#✓ TFI questionnaire — 2026-05-12]]. `TfiPromptCard`'s "시작 →" button was a `// TODO` in `HomeScreen.kt`; now wires through a new `onStartTfi` callback. `MainActivity` hands it `{ tfiVm.reset(); current = Tab.Settings; settingsSub = SettingsSub.Tfi }` — reuses the existing TFI rendering inside `Tab.Settings` instead of duplicating it under Home. Closing or submitting the questionnaire follows the same `settingsSub = null` / `SettingsSub.TfiResults` paths as the Settings entry point.

Cadence gating: `HomeScreen` now takes `showTfiPrompt: Boolean`; the card is only rendered when due. `MainActivity` computes `System.currentTimeMillis() >= lastTfiDate + tfiCadenceWeeks × 7 × 24 × 60 × 60 × 1000` against the `lastTfiDate` / `tfiCadenceWeeks` flows already exposed by `UserSettingsRepository`. `lastTfiDate = 0L` (no prior assessment) trivially satisfies the inequality, so first-run users still see the prompt without a special case.

Files: `ui/home/HomeScreen.kt` (HomeScreen + TfiPromptCard signatures, conditional render), `MainActivity.kt` (lastTfiDate collect, due-ness compute, HomeScreen call site).

### ✓ ListeningSession minimum-duration floor 5 s → 3 min — 2026-05-14

`ListeningSessionRepository.MIN_DURATION_MS` raised from 5 000 to 180 000. Heatmap was over-counting low-signal short plays; 3 min is the deliberate compromise against Gemini's proposed 5-min floor (which would silently drop legitimate short TRT exposures). `wiki/app/architecture.md` § ListeningSession floor note updated to reflect the new value as shipped (vs. "planned").

### ✓ 기록 (Records) calendar — heatmap month grid — 2026-05-13

`IMG_2486.PNG` mock-driven redesign of the 기록 tab. Replaces the "준비 중" stub from the [[#✓ 4-tab nav shell — 2026-05-11]] task with a real month-grid heatmap calendar where day-cell intensity encodes daily listening duration. Re-toned to Teal (mock was coral) for app-wide chrome coherence; today indicator is a 2 dp Teal outline ring; weekly summary cards and TFI history deferred to follow-ups.

**Data layer — new `ListeningSession`.** Room v2 → v3 additive migration (`CREATE TABLE IF NOT EXISTS listening_sessions`). Entity (`id, startedAtEpochMs, endedAtEpochMs, durationMs, presetLabel?`), `ListeningSessionDao` (`insert / observeRange / deleteById`), `ListeningSessionRepository` (wraps DAO, exposes `logSession()` with a 5-second minimum-duration filter to drop accidental tap-then-tap sequences). The `presetLabel` column is nullable in v1 and always written `null`; populated once presets ship (Next #6).

**AudioRepository session logging.** Constructor now takes `ListeningSessionRepository` + holds a private IO scope. `start()` records `currentSessionStartedAtMs`. `stop()` and the `AUDIOFOCUS_LOSS` branch finalise the session via `sessionRepo.logSession(start, now)` from the IO scope. `LOSS_TRANSIENT` does NOT split a session — pausing for a call and resuming counts as one session for the heatmap. Logs on success: `AudioRepository: logged session id=N duration=Xms`.

**`RecordsViewModel`.** Owns visible `YearMonth` (mutable via `previousMonth() / nextMonth()`). For each month, builds the 6×7 (42-cell) grid starting Sunday, bucket-sums `ListeningSession.durationMs` per `LocalDate` (start-of-day in `ZoneId.systemDefault()`), and computes `monthTotalMs` + `maxDayMs` over in-current-month cells only (excludes spillover so an outlier in a neighbouring month doesn't flatten the ramp). State flows reactively from `sessionRepo.observeRange(monthGridStart, monthGridEndExclusive)` via `flatMapLatest`.

**`RecordsScreen`.** Cream `Bg` background, "기록" page header, then a single white card (`Surface`, 12 dp radius, soft 2 dp shadow):
- Card header: "기록 달력" + `< MMM yyyy >` chevron group (`pressableClickable`).
- Weekday row S M T W T F S (English single-letter, Sunday-anchored, matches mock).
- 6×7 day grid with `.aspectRatio(1f)` cells, 4 dp gaps, 8 dp corner radius. Today gets a 2 dp `Teal` border; high-intensity cells render the date in white for legibility (when `totalMs > 0.5 × maxDayMs`).
- Legend row: "적음 ▢▢▢▢ 많음" (4 swatches in `Heat1..Heat4`) on the left, "이번 달: Xh Ym" on the right.

**Heatmap ramp.** Added 5 tokens to `Color.kt`: `Heat0` (= `Color(0xFFEFEAE0)` — a touch darker than `Bg` so empty cells still read as cells; also used for spillover days), `Heat1..Heat3` (interpolated teals), `Heat4` (= existing `Teal`). Bucketing rule normalises by in-month max: ratio `(0, 0.25]` → Heat1, `(0.25, 0.50]` → Heat2, `(0.50, 0.75]` → Heat3, `>0.75` → Heat4.

**Wiring.** `MainActivity.kt` instantiates `ListeningSessionRepository(db.listeningSessionDao())`, threads it into the `AudioRepository(...)` constructor, hoists `RecordsViewModel` via `viewModel(factory = RecordsViewModelFactory(sessions))`, and passes it into `RecordsScreen(recordsVm)` (was previously `RecordsScreen()` with no params).

**Verified 2026-05-13 on emulator-5554** (install over a v2 install with existing TFI data):
- Migration ran cleanly — `listening_sessions` table created with the expected schema; `diary_entries` + `tfi_assessments` data preserved (TFI count = 2 pre/post); v1 sanity check still green (`RoomScaffold: wrote id=13 readBack='room-scaffold-sanity-check'`).
- Tapped play on Matcher, waited 15 s, tapped stop → logcat `AudioRepository: logged session id=1 duration=15054ms`; sqlite shows `1|1778641022594|1778641037648|15054`.
- Tapped play, immediate stop (~2 s) → no row written (5-second filter working).
- 기록 tab renders calendar card. Today's cell (5월 13일 수요일) shows the Teal-4 fill from the 15 s session (the only data point, so ratio = 1.0 → Heat4); white date text legible.
- Right chevron → June 2026 renders empty (`Heat0` in-month cells, spillover days greyed) with legend "이번 달: 0m".
- Left chevron → May returns with the 13일 cell still filled.
- Zero `AndroidRuntime FATAL` across the run.

Not in this task (deferred — see Next): 주간 요약 weekly cards (now Now), day-tap drilldown, TFI history trend, streak/goal-progress badge (needs `daily_listening_goal_min` setting), preset label on logged sessions.

### ✓ TFI questionnaire — 2026-05-12

25-item Tinnitus Functional Index — primary outcome measure, MCID = 13 points. Subscale-grouped pagination (8 pages, one per clinical subscale) + scoring + persistence + Settings entry points. Korean translations are a v1 draft pending clinician review.

**Data layer.** New `TFIAssessment` entity (`id, takenAtEpochMs, items, totalScore, subscaleScores`) stored as JSON columns via a `MapTypeConverters` helper — no separate items table. Room v1 → v2 additive migration (`CREATE TABLE IF NOT EXISTS tfi_assessments`); `DiaryEntry` table untouched, v1 sanity check (`RoomScaffold` tag) still green post-migration. New `TfiAssessmentDao` (`upsert / latest / observeAll / deleteById`) and `TfiRepository` that wraps the DAO + bumps `last_tfi_date` on submit. New DataStore keys `TFI_CADENCE_WEEKS` (int, default 2) and `LAST_TFI_DATE` (long epoch ms, default 0); `UserSettingsRepository` exposes them as Flows + suspend setters.

**Scoring.** `ui/assessment/TfiScoring.kt` — pure-Kotlin helper. 8 subscales (I, SC, C, SL, A, R, Q, E), Korean labels (`침투성, 통제감, 인지, 수면, 청각, 휴식, 삶의 질, 정서`). Total = mean of valid items × 10 (rounded, clamped 0–100); subscales same formula per group. Spec's "≥ 19 of 25 items" validity flag carried defensively (UI gates "다음" on all-answered so it's never actually triggered). Meikle 2012 severity bands surfaced on the results screen (0–17 경미, 18–31 경도, 32–53 중등도, 54–72 심함, 73–100 매우 심함) — Korean phrasing pending clinician review.

**Content.** `ui/assessment/TfiContent.kt` — the 25 items with Korean prompts + per-item anchor labels (e.g. `전혀 방해되지 않음 ↔ 완전히 방해됨`). All items share a `지난 한 주 동안…` instruction stem rendered once at the top of each page (omitted from per-item text for readability). Items #1 and #3 are 0–100 percentage in the source instrument; we present 0–10 uniformly across all items so the scoring helper needs no per-item rescaling (anchor labels carry the percentage meaning).

**UI.**
- `TfiQuestionnaireScreen.kt` — 8-page `HorizontalPager`. Sticky top: `← 닫기` + `n / 8` + thin teal progress bar. Body: subscale eyebrow + 3 (or 4 for `Q`) cards, each with question text + 0–10 row + anchor labels. Active button: filled `Teal` + white bold + teal spot shadow; inactive: transparent + `Ink2`. Bottom CTA: disabled (TealSoft + Muted) until every question on the page has a value; the final page swaps `다음 →` for `제출하기`.
- `TfiResultsScreen.kt` — `결과` eyebrow + `TFI 점수` headline. White Total card: `XX / 100` + severity pill (`TealSoft` bg + `Teal` text). 세부 점수 card with 8 subscale rows (label + score + horizontal `Teal` fill bar to `score / 100 %`). Footer disclaimer (`TFI 점수는 0–100 범위로, 점수가 낮을수록 이명의 영향이 적음을 의미합니다.`) + full-width `완료` CTA.
- `TfiQuestionnaireViewModel.kt` — holds `responses: SnapshotStateMap<Int,Int>` and the last submitted `TFIAssessment`; `submit()` calls the scoring helper via the repo and writes the row + `last_tfi_date`.

**Wiring.**
- `MainActivity.kt` — instantiates `TfiRepository(db.tfiAssessmentDao(), userSettingsRepository)`; new `enum class SettingsSub { Tfi, TfiResults }` mirroring the existing `SoundSub` pattern; routing inside `Tab.Settings` switches between root / Tfi / TfiResults. `BackHandler` collapses sub → null. Switching tabs from 설정 also collapses the sub.
- `SettingsScreen.kt` — new params `onOpenTfi`, `tfiCadenceWeeks`, `onToggleTfiCadence`. The previously-disabled `ValueRow("TFI 주기", "2주마다", enabled = false)` is replaced with `TfiCadenceRow` — tap-to-cycle 2주 ↔ 1주, persists via DataStore. New `ActionRow("TFI 다시 작성", "작성하기", onClick = onOpenTfi)` directly beneath it.

**Korean copy register.** User-facing flow strings use the generic 설문조사-style register (`지난 한 주 동안…`, `다음 →`, `제출하기`, `완료`). `TFI` appears only on Settings rows (`TFI 주기`, `TFI 다시 작성`) and the Results headline (`TFI 점수`), where the user is in instrument-aware surfaces. Severity labels (`경미 / 경도 / 중등도 / 심함 / 매우 심함`) are translation drafts.

**Verified 2026-05-12 on emulator-5554**:
- Build green (`:app:assembleDebug`, KSP + compile + APK), clean install over v1.
- Migration ran cleanly — `tfi_assessments` table created, `diary_entries` preserved, v1 sanity check still passes (`RoomScaffold: wrote id=12 readBack='room-scaffold-sanity-check'`).
- 설정 renders new layout. `TFI 주기` tap cycles `2주마다 ↔ 1주마다`, persists across `am force-stop` + relaunch (`tfi_cadence_weeks` key visible in `user_settings.preferences_pb`).
- `TFI 다시 작성` → questionnaire page 1/8 ("침투성") with 3 cards + `← 닫기` + `1 / 8`. `다음 →` disabled until all 3 answered, then enables (TealSoft → Teal).
- Walked 8 pages via UI-dump scripting. Page 7 (`삶의 질`) correctly renders 4 question cards; page 8 CTA swaps to `제출하기`.
- Submit → Results screen renders total `51 / 100` + `중등도` severity pill + 8 subscale bars. SQLite (`tfi_assessments`) shows `{id:1, totalScore:51, items:{1..25:...}, subscaleScores:{"I":53,...,"E":50}}` — totals algebraically consistent with stored items (mean × 10).
- `완료` → returns to Settings; cadence still `1주마다`.
- Zero `AndroidRuntime FATAL` across the run.

**Not in this task** (explicit follow-ups, see [[#Next (queued, in order)]]): Home `TfiPromptCard` button wiring (still a TODO at `HomeScreen.kt:469-471`); cadence-based gating of the Home prompt card; bottom-sheet cadence picker (v1 is tap-to-cycle); 기록 (Records) trend chart over `observeAll()`.

### ✓ Tactile polish pass — press effects + polished play button — 2026-05-12

Reusable touch-feedback primitives in a new `ui/theme/PressEffect.kt`, then every tappable surface across the 5 redesigned screens converted to use them. No feature scope change — purely "every button now feels like a button."

New primitives:
- `Modifier.pressableClickable { … }` — drop-in replacement for `.clickable`. Adds a snappy press-scale (default 0.96) via `graphicsLayer` + a `MutableInteractionSource` listener; spring `dampingRatio=0.62 / stiffness=820`. Must be placed **first** in the modifier chain so chrome + content scale together.
- `PolishedPlayButton` — hero CTA play button. Layered render (drop shadow → gradient face → top gloss → icon → inner rim), 0.93 scale + shadow tuck on press, 76 dp default, takes a `variant` param.
- `PlayButtonVariant` — `Coral` (light/cream cards — 홈 hero) and `Orange` (`DarkBg` studio — matcher).

Conversions:
- `HomeScreen.kt` — hero play button → `PolishedPlayButton(variant = Coral)`; settings gear, sleep-timer row, TFI prompt → `pressableClickable` + soft shadows.
- `FrequencyMatchingScreen.kt` — play button → `PolishedPlayButton(variant = Orange)`; matches the dark studio palette.
- `SoundSettingsScreen.kt` — preset switcher "+ 저장", "다시 측정 ›" pill, color-noise pills, processing-mode segmented control, ambient-mix preview — all get `pressableClickable` + subtle 1–2 dp shadows; segmented-control segments restructured (`Surface` wrapping a `clickable Box` → flat `Box` with shadow modifier directly, which lets the press-scale actually scale the chrome).
- `OnboardingScreen.kt` / `SettingsScreen.kt` — small `clickable → pressableClickable` swaps on the few interactive surfaces.

Files: new `ui/theme/PressEffect.kt`. Modified: `ui/home/HomeScreen.kt`, `ui/matcher/FrequencyMatchingScreen.kt`, `ui/sounds/SoundSettingsScreen.kt`, `ui/onboarding/OnboardingScreen.kt`, `ui/settings/SettingsScreen.kt`.

Not in this task: haptic feedback (`performHapticFeedback`) on press — deferred until we have a clearer policy on which surfaces warrant it; ripple-over-press-scale combo (the default `indication = null` argument is the lever if a particular surface ever needs both).

### ✓ Visual system migration + 홈 (Home) Direction A redesign — 2026-05-12

Theme migration applied across the chrome: status bar switched to light appearance (dark icons on cream), `NavigationBar` swapped to white surface + teal active + `TealSoft` indicator + muted inactive, `OrangeAccent` dropped from all screens. Matcher subscreen kept dark (still a "lab" surface).

홈 rebuilt per wireframes Direction A + D (player-first hero + week strip):
- Top bar — greeting + 치료 N주차 + settings gear (routes to 설정).
- Hero player card — dark gradient, "저녁 휴식" preset label, big play button (routes to 소리), `84 / 120 min` overlay.
- Week strip — 7 columns; past days = checkmark, today = gradient bar, future = empty.
- Sleep-timer chip (`30분`, no-op v1).
- TFI prompt card — coral-soft bg + "시작 →" button; wired up once [[#Now]] lands.

Sibling screens (`RecordsScreen`, `SettingsScreen`, `SoundSettingsScreen`) updated to inherit the new light theme correctly. Files: `MainActivity.kt`, `ui/theme/Theme.kt`, `ui/home/HomeScreen.kt`, `ui/records/RecordsScreen.kt`, `ui/settings/SettingsScreen.kt`. Commit `7fcbfa2`.

Not in this task: actually wiring the week strip / hero progress / TFI prompt to real data (each waits on its own task).

### ✓ 소리 (Sound) page redesign — 2026-05-12

Replaced the 4-card stub (`ActiveCard` + 3 `PlaceholderCard`s from the nav-shell task) with wireframes Direction A (Calm settings list):
- Header row — "소리 설정" + "미리듣기 ▶" preview button.
- Preset switcher card (TealSoft bg) — "저녁 휴식 ▾" + ghost "+ 저장" button.
- Frequency card — "4,250 Hz · 고음" + TealSoft "다시 측정 ›" pill (opens the matcher subscreen — only live wiring on the page).
- Processing-mode segmented control (`노치` active / `증폭`).
- Color-noise pill row (`핑크` active / `화이트` / `브라운` / `끄기`).
- Ambient-mix progress bars (빗소리 60 %, 파도소리 0 %).

All non-frequency values are display-only with `TODO` markers — the visual scaffold lands first; persistence + audio routing for color noise / ambient mix / presets follows in their own tasks. `MainActivity` and the matcher subscreen are untouched. Commit `5104bd7`.

### ✓ Onboarding Direction A redesign — 2026-05-12

Re-skinned the 7-page onboarding from dark+orange to Direction A (cream + teal + coral) per wireframes — addresses the earlier critique ("no imagery, off-brand pill, weak dots, no visual rhythm"). Hero height 320 dp; section-specific Canvas art replaces the flat color block so each section has visual identity:
- Pages 1–3 (`1. 이명에 대해`) — waveform pattern.
- Pages 4–6 (`2. 치료 접근`) — brain-node network (6 nodes + central coral node).
- Page 7 (`3. 앱 사용법`) — checkmark arc + progress dots.

Hardcoded `Color(0xFFE8F0EE)` replaced with the `TealSoft` design token; hero composable now takes a `section` param to route to the right art block. Pager / page-dot indicator / Skip / Next / 이전 / 시작하기 controls + all clinical copy retained from the original onboarding tutorial task. File: `ui/onboarding/OnboardingScreen.kt`. Commit `3a15c1e`.

### ✓ 4-tab nav shell — 2026-05-11

`MainActivity.Tab` grew from `{ Matcher, Settings }` → `{ Home, Sound, Records, Settings }` per [[wiki/app/navigation]]. `FrequencyMatchingScreen` is no longer a tab — it's reached only from inside 소리 via a "주파수 매칭 및 사운드 재생" card. Default landing = 홈. Subscreen state inside 소리 is a single-level `mutableStateOf<SoundSub?>` + `BackHandler` (no nav-library dep yet); switching tabs collapses any open subscreen.

New stubs (final packages per [[wiki/app/architecture]]):

- `ui/home/HomeScreen.kt` — "준비 중" + "곧 일일 청취 진도와 빠른 재생이 추가됩니다" + a prominent "사운드 치료 시작하기" CTA that hoists the parent tab to 소리.
- `ui/sounds/SoundSettingsScreen.kt` — one real `ActiveCard` opens the matcher subscreen; three disabled `PlaceholderCard`s (`컬러 노이즈`, `자연음 믹스`, `저장된 프리셋`) mirror the structure that lands in Next #2 so the screen doesn't feel empty.
- `ui/records/RecordsScreen.kt` — "준비 중" stub + three preview chips (`달력`, `청취 로그`, `TFI 추이`).

Nav icons: `Home / GraphicEq / CalendarMonth / Settings`. Tab + subscreen state are `rememberSaveable` to survive rotation. `SettingsScreen` is unchanged for this task — full expansion lives in its own later slot.

**Verified 2026-05-11 on emulator-5554** (full `pm clear` + reinstall):
- Clean install → onboarding page 1.
- 건너뛰기 → lands on 홈 (default tab), all 4 tabs render in bottom nav.
- 홈 CTA → switches to 소리 root with `ActiveCard` + 3 placeholders.
- Tap `ActiveCard` → `FrequencyMatchingScreen` opens; system back returns to 소리 root (not app exit).
- 기록 → "준비 중" + preview chips. 설정 → "튜토리얼 다시 보기" row intact (regression check passed).
- Zero `AndroidRuntime` FATAL across the run.

Not in this task: actually building any of 홈 / 기록 / 소리 expansion (their own Next items); M3 theme unification across tabs (Next #1).

### ✓ Onboarding tutorial — 2026-05-11

7-page swipeable onboarding (HorizontalPager) gating the app on first launch. Dark theme matching the Matcher screen the user lands on after finishing. Korean copy synthesized from `wiki/clinical/*` — each page has a `// Source: wiki/clinical/<file>.md` comment for traceability.

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

Files: new `ui/onboarding/OnboardingScreen.kt` (HorizontalPager + page-dot indicator + Skip/Next/이전/시작하기 controls + content data class), `ui/onboarding/OnboardingViewModel.kt` (+ factory). Modified: `data/repository/UserSettingsRepository.kt` (added `onboardingComplete: Flow<Boolean>` + `setOnboardingComplete()`); `MainActivity.kt` (gates `OnboardingScreen` vs `RootScaffold` on `onboardingComplete`; `replayOnboarding` local state lets Settings re-trigger the flow without flipping the persisted flag); `ui/settings/SettingsScreen.kt` (added "튜토리얼 다시 보기" row + signature now takes `onReplayOnboarding: () -> Unit`).

`PreferenceKeys.ONBOARDING_COMPLETE` already existed but had never been read — this task wires it up. `tinnitus_explainer_seen` (planned in architecture.md) deferred — redundant with `ONBOARDING_COMPLETE` for v1.

**Verified 2026-05-11 on emulator-5554** (clean reinstall):
- Cold start with no prefs → onboarding page 1 visible (`1 / 7`, badge "1. 이명에 대해", title "이명은 무엇인가요?", Skip + Next buttons present).
- 다음 × 6 → page 7 (`7 / 7`, badge "3. 앱 사용법", CTA swapped to "시작하기").
- 시작하기 → app lands on the Matcher tab; `onboarding_complete` key now present in `/data/data/com.tinnitustracker/files/datastore/user_settings.preferences_pb`.
- `am force-stop` + relaunch → goes straight to Matcher, no onboarding flicker.
- 설정 tab → "튜토리얼 다시 보기" row visible → tap → onboarding re-appears at page 1.
- 건너뛰기 (Skip) from re-launched onboarding → returns to Matcher cleanly.
- Zero `AndroidRuntime` FATAL entries across the run.

Not in this task (deferred): red-flag screener (separate later task); sleep-timer / sound-settings module guides (those modules don't exist yet).

### ✓ Audio focus & interruption handling — 2026-05-11

New `audio/routing/AudioFocusController.kt` — single `AudioFocusRequest` (USAGE_MEDIA + CONTENT_TYPE_MUSIC; `setWillPauseWhenDucked(true)` so we pause cleanly instead of letting the system attenuate therapeutic noise). Listener mirrors LOSS / GAIN into its own `hasFocus` flag so a subsequent `request()` doesn't short-circuit on stale state. `AudioRepository.start()` now requests focus before touching the FGS / engine; `stop()` abandons.

Interruption policy (in `AudioRepository.onFocusChange`):
- `LOSS` → full teardown: engine off, service stopped, latch cleared (no spurious auto-resume later).
- `LOSS_TRANSIENT` / `LOSS_TRANSIENT_CAN_DUCK` → engine off, **service stays in foreground**, latch (`wasPlayingBeforeInterruption`) set. Notification remains visible.
- `GAIN` after a transient → engine restarts automatically, latch cleared.

Files: new `audio/routing/AudioFocusController.kt`. Modified: `data/repository/AudioRepository.kt` (focus controller + focus-change handler + start/stop hooks).

**Verified 2026-05-11 on emulator-5554** with simulated GSM call:
- Tap 재생 → service `isForeground=true types=0x00000002`, focus acquired silently.
- `adb emu gsm call 5551212` → logcat `AudioRepository: focus LOSS_TRANSIENT → pause engine, keep service`; service still `isForeground=true`.
- `adb emu gsm cancel 5551212` → logcat `focus GAIN after transient loss → resume engine`; service still up.
- Tap 정지 → service torn down, focus abandoned.
- Tap 재생 again → focus re-acquired cleanly (proves the listener's `hasFocus` mirror is working).
- No FATAL / AndroidRuntime exceptions across the run.

Not in this task: route changes (BECOMING_NOISY / headphone unplug — separate `audio/routing` follow-up); MediaSession lock-screen controls.

### ✓ Implement `TherapyAudioService` (foreground service) — 2026-05-11

Pattern chosen: **Option A** — `AudioEngine` is now a process-singleton on the new `TinnitusTrackerApp : Application` class; foreground service is started-only (no binding); `AudioRepository` wraps everything as a drop-in replacement for direct `AudioEngine` usage in the ViewModel. Rationale and rejected alternative (canonical bound + started) in `/Users/midnight/.claude/plans/luminous-baking-rocket.md`.

Files: new `TinnitusTrackerApp.kt`, `audio/service/TherapyAudioService.kt`, `audio/service/AudioNotification.kt`, `data/repository/AudioRepository.kt`, `res/drawable/ic_notification_wave.xml`. Modified: `AndroidManifest.xml` (added 3 permissions, `<service foregroundServiceType="mediaPlayback">`, `android:name=".TinnitusTrackerApp"`, `MainActivity launchMode="singleTop"`), `MainActivity.kt` (dropped engine ownership; instantiates `AudioRepository`), `FrequencyMatchingViewModel.kt` + factory (1-token rename `engine → audio`, type swap to `AudioRepository`; `togglePlay()` delegates).

Notification design: single ongoing low-importance notification, channel `therapy_audio`, NOTIF_ID 1001, one "정지" action via `ACTION_STOP` PendingIntent, tap → singleTop `MainActivity`. No pause-but-resumable state in v1 (notification exists iff playing — simpler, fewer bug surfaces).

**Verified 2026-05-11 on emulator-5554**:
- Build green (`:app:assembleDebug`, KSP + compile + APK).
- Launch → no crash, Room sanity check still passes (`RoomScaffold` id=2).
- Tap 재생 → `dumpsys activity services` shows `isForeground=true types=0x00000002 (mediaPlayback) foregroundId=1001 channel=therapy_audio flags=ONGOING|FOREGROUND_SERVICE actions=1`.
- HOME (background) → service still `isForeground=true`, process alive (PID survived backgrounding).
- Tap 정지 → service gone, `cmd notification list` empty for our package.
- No FATAL / AndroidRuntime exceptions in logcat.

Not in this task (separate Next items): audio focus / interruption handling; MediaSession lock-screen controls; runtime POST_NOTIFICATIONS request UX; resumable-pause state.

### ✓ Wire up Room database — 2026-05-11

Room 2.6.1 + KSP wired in `app/build.gradle.kts`. `AppDatabase` singleton v1 (`exportSchema = true`, schema lands in `app/schemas/`). First entity `DiaryEntry` + `DiaryDao` (`upsert / observeRange / latest / deleteById`). `DiaryRepository` thin wrapper, manually injected in `MainActivity` next to `UserSettingsRepository`. Non-residual round-trip sanity check (`upsert → latest → deleteById`) runs on every cold start under tag `RoomScaffold`. **Verified 2026-05-11 on emulator-5554** — logcat shows `id=1, readBack='room-scaffold-sanity-check', dbFile=/data/user/0/com.tinnitustracker/databases/tinnitus_tracker.db`; DB file confirmed via `run-as`. Orphan files from previous DB name (`tinnitus_tracker_database*`, 2026-02-01) removed.

### ✓ Build & extend the tinnitus frequency selecting page — base 2026-05-08, extensions 2026-05-11

**Base (2026-05-08):** `FrequencyMatchingScreen.kt` + `FrequencyMatchingViewModel.kt`. Logarithmic dial (100 Hz – 16 kHz), volume hard-cap at 70 %, octave-confusion check (auto-scrolls into view after 2 s of inactivity), DataStore persistence via `UserSettingsRepository`, "can't find pitch" skip affordance. Stepper buttons ±10 / ±100 Hz; all inputs snap to nearest 10 Hz.

**Extensions (2026-05-11)** — per `raw/Plan(수기).md` §3:
- **Notch / amplify mode selector** — new `PROCESSING_MODE` string preference; `ProcessingModeCard` shown via `AnimatedVisibility` when `hasTonalTinnitus == true`; selection persists to DataStore and updates `AudioEngine.Mode` (`MASK = 증폭`, `NOTCH = 노치`).
- **Seamless matching → therapy handoff** — `confirmPitch()` switches the engine to the saved processing mode mid-stream; `BiquadFilter.reset()` runs on topology change so there is no audible click.

---

## See also
- [[wiki/app/prd]]
- [[wiki/app/architecture]]
- [[wiki/tasks]] — wiki maintenance tasks (separate from app implementation)
