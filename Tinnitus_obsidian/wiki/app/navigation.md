---
title: "App Navigation & Screen Layouts"
type: app
aliases: ["Navigation", "Screen Layouts", "UI Layout"]
tags: [app/design, app/ui]
evidence: n/a
status: draft
sources: ["[[wiki/sources/kole-jain-genius-ux]]", "[[wiki/sources/kole-jain-beginner-mistakes]]"]
last_reviewed: 2026-05-12
---

# App Navigation & Screen Layouts

How the app is structured screen-by-screen, what each screen holds, and where elements sit on it. Downstream of [[wiki/app/prd]] (scope) and [[wiki/app/plan]] (sequence); upstream of any new `ui/*` package work.

## Framing — why this doc exists

The matcher (`ui/matcher/FrequencyMatchingScreen`) is currently the entry tab, but it is a **one-time setup task**: the user matches their pitch once, the result is persisted to DataStore (`matched_frequency_hz`, `has_tonal_tinnitus`, `processing_mode`), and from that point forward the matcher only re-opens when the user wants to re-measure. What the user actually opens the app for every day — for the 12–24 month TRT arc — is the **sound-therapy session**: hit play, accumulate minutes against today's listening goal, sleep-timer for evening use, periodic TFI re-take.

The current 2-tab nav (`톤 찾기` / `설정`) optimizes for the one-time task. The post-MVP nav needs to optimize for the daily one.

## Top-level navigation

Four-tab bottom nav. Each tab maps to one or more MVP buckets from `raw/Plan(수기).md`:

| Tab | Korean | Maps to (handwritten plan) | Status |
|---|---|---|---|
| Home | 홈 | (daily use — not in the original 1–5; emerges from §5) | Planned |
| Sound | 소리 | §3 sound settings (matcher, notch/amplify, color noise, ambient mix, presets) | Partial — matcher only |
| Records | 기록 | §5 records page (calendar, listening log, weekly avg, TFI history) | Planned |
| Settings | 설정 | meta (tutorial re-watch, mechanism explainer, TFI re-take, theme, about) | Placeholder |

Onboarding (§1) + mechanism explainer (§2) + TFI questionnaire (§4) are not tabs — they are full-screen flows triggered from 홈 or 설정.

### Why four, not three or five

- **Three** (collapsing 홈 into 소리) would force the daily-progress UI into the same screen as preset editing, which mixes two different mental modes (passive consumption vs. configuration).
- **Five** (e.g. splitting 다이어리 out from 기록) would push tabs into the cramped zone on small phones. Diary is a low-frequency journaling action; it lives as a row link on 홈 and gets a sheet, not a tab.

## First-launch flow vs. returning open

**First launch** (cold install, `onboarding_complete = false`):

```
Splash → Onboarding tutorial → Tinnitus mechanism explainer
     → Matcher (or skip → "skipped" state)
     → Sound-settings first-run (pick a starter preset)
     → Set daily_listening_goal_min (default 120)
     → Land on 홈
```

The first-launch flow runs once. `onboarding_complete = true` is written at the end. The mechanism explainer is re-viewable from 설정 (per [[wiki/app/plan]] §1).

**Returning open** (every subsequent launch): always lands on 홈. The matcher is only reached deliberately, via 소리 → "주파수 다시 측정". The user *never* sees a stale-state 홈 (no preset, no listening data) because first-launch guarantees both exist.

## Screen — 홈 (Home)

The daily landing screen. Designed around the question: _"did I do my therapy today, and can I start a session in one tap?"_

```
┌─────────────────────────────────────┐
│  안녕하세요              ⚙ 설정     │  ← thin top bar
│  치료 3주차 · 5월 11일 (월)        │
│                                     │
│           ╭─────────────╮           │
│         ╱     84 / 120    ╲        │  ← listening-progress ring
│        │       minutes      │       │     (centerpiece, ~220 dp)
│        │     오늘 청취량      │       │
│         ╲                 ╱        │
│           ╰─────────────╯           │
│                                     │
│              ●─────●                │  ← primary play / stop button
│             (   ▶   )              │     (orange, ~64 dp, same
│              ●─────●                │      visual as matcher's)
│                                     │
│  ┌─────────────────────────────┐  │
│  │ 현재 설정         [편집  ›] │  │  ← active preset card
│  │ "저녁 휴식"                  │  │     name + chip row
│  │ 노치 · 핑크 노이즈 · 빗소리 │  │     tap edit → 소리 tab
│  └─────────────────────────────┘  │
│                                     │
│  ⏱ 수면 타이머: 30분           ›  │  ← sleep timer chip → sheet
│                                     │
│  ┌─────────────────────────────┐  │
│  │ 📋 이번 주 설문조사          │  │  ← TFI prompt (CONDITIONAL)
│  │ 약 5분 소요   [시작하기 ›] │  │     hidden when not due
│  └─────────────────────────────┘  │
│                                     │
│  오늘의 기록 작성하기            ›  │  ← diary entry shortcut
│                                     │
└─────────────────────────────────────┘
   홈      소리      기록      설정
   ●
```

### Element placement rationale

| Element | Position | Why here |
|---|---|---|
| Greeting + treatment-week counter | Top bar | TRT is a multi-month arc. "치료 3주차" framing reinforces patient understanding of timeline (per [[wiki/clinical/trt]]). |
| Settings cog (top-right) | Top bar | Optional escape hatch — 설정 is also a tab, so this is for users who don't notice the bottom nav. Can be omitted in v1.0. |
| Listening-progress ring | Center-top, dominant | The single most important daily question is "did I hit my goal?" Above the play button so the goal is visible *before* the action. Inside ring: `minutes_today / daily_listening_goal_min` from DataStore. Tap → 기록 tab. |
| Play / stop button | Center, below ring | Primary daily action. Thumb-reachable. Same visual as `FrequencyMatchingScreen`'s play button so the audio engine feels unified across screens. Tapping starts the *active preset's* sound therapy (not the matcher tone). |
| Active preset card | Mid-screen | Answers "what am I about to play?" without leaving 홈. Edit affordance → 소리 tab. Pulls from `active_preset_id` DataStore key + `SoundPreset` Room row. |
| Sleep timer chip | Below preset | Common but not central. Bedtime users will hunt for it; daytime users shouldn't have it dominate. Tap → bottom sheet with 15 / 30 / 60 / 90 / 끄지않음. Last-used value persisted in DataStore. |
| TFI prompt card | Conditional, below sleep timer | Only renders when `today >= last_tfi_date + tfi_cadence_weeks`. Otherwise the slot collapses entirely — non-survey days stay calm. Tap → TFI questionnaire flow. |
| Diary entry shortcut | Bottom row link | Diary is opt-in journaling, not required. Row-link weight is intentional — inline sliders would imply daily friction. Tap → diary sheet. |

### State variations of 홈

- **First-of-day** (no minutes logged today): ring at 0 / 120, play button is the obvious call-to-action.
- **Mid-session** (engine playing): play icon becomes stop, ring increments live, sleep-timer chip can show "23분 남음" countdown if armed.
- **Goal-met** (minutes ≥ goal): ring fills with a subtle celebratory ring color (still calm — TRT is not a gamified habit tracker). Streak count appears as a small badge if applicable.
- **TFI overdue** (more than `tfi_cadence_weeks` past `last_tfi_date`): TFI prompt card renders. If user dismisses, it returns the next day until completed.

## Screen — 소리 (Sound)

Sound-therapy configuration. Promotes the current matcher tab into a broader settings page that holds matcher + processing mode + color noise + ambient mix + saved presets. Maps to handwritten plan §3.

```
┌─────────────────────────────────────┐
│  소리 설정                          │  ← top bar
│                                     │
│  현재 프리셋:  "저녁 휴식"       ▾  │  ← preset switcher dropdown
│  [ 새 프리셋으로 저장 ]              │     (also: save-as button)
│                                     │
│  ┌─ 이명 주파수 ──────────────────┐ │
│  │  4,250 Hz · 고음              │ │  ← summary card; not the
│  │  [ 다시 측정 ›]                │ │     full matcher — that's
│  └────────────────────────────────┘ │     a subscreen reached
│                                     │     via "다시 측정"
│  ┌─ 처리 방식 ────────────────────┐ │
│  │  ⦿ 노치       ○ 증폭          │ │  ← mirror of matcher's
│  │  광대역 노이즈에서 이명 주파수를 │ │     ProcessingModeCard
│  │  차단해 측방 억제을 유도합니다.  │ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌─ 컬러 노이즈 ──────────────────┐ │
│  │  ● 핑크   ○ 화이트   ○ 브라운 │ │  ← color noise picker
│  │  ○ 끄기                        │ │     (small ▶ per option
│  │                    [▶ 미리듣기]│ │      for preview)
│  └────────────────────────────────┘ │
│                                     │
│  ┌─ 자연음 믹스 ──────────────────┐ │
│  │  🌧 빗소리          ●━━━○ 60%│ │  ← ambient layers
│  │  🌊 파도소리        ○─── 0%  │ │     toggle + gain slider
│  │  ⛈ 천둥             ○─── 0%  │ │     per source
│  │  💧 물소리           ○─── 0%  │ │
│  │  🍃 바람             ○─── 0%  │ │
│  │  🐦 새소리           ○─── 0%  │ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌─ 저장된 프리셋 ────────────────┐ │
│  │  • 저녁 휴식        (active)  │ │  ← saved SoundPresets list
│  │  • 아침 집중                   │ │     tap to load, long-press
│  │  • 수면용 (낮은 볼륨)         │ │     to delete
│  └────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
   홈      소리      기록      설정
            ●
```

### Element placement rationale

| Element | Position | Why here |
|---|---|---|
| Current preset switcher | Top, sticky if scrolled | Anchors the entire screen — every control below modifies *this preset*. Save-as button is right next to it because edits are non-destructive (must explicitly save). |
| Frequency summary | First settings card | Most users won't re-measure often. A summary + "다시 측정" link is enough; the full matcher (dial + knob + octave check) lives behind that link, not inline here. |
| Processing mode | Second card | Mirrors `ProcessingModeCard` from the matcher; same `AudioEngine.Mode` underlying state. Keeps the matcher → therapy handoff seamless. |
| Color noise | Third card | Pink/white/brown/none, with preview play per option (short loop). The mix below layers on top of this. |
| Natural-sound mixer | Fourth card | Each ambient layer is a toggle + gain slider (0–100%). Stored as list of `{source, gain}` in the `SoundPreset.ambientLayers` field per [[wiki/app/architecture]]. |
| Saved presets list | Bottom | Lists `SoundPreset` rows from Room. Tap = load (writes `active_preset_id`). Long-press = delete confirmation. Active preset is visually marked. |

### Subscreens of 소리

- **Matcher** (`ui/matcher/FrequencyMatchingScreen`) — reached via "다시 측정". Already exists. Returning from the matcher updates the frequency summary card.
- **Preset save dialog** — name input + save button. Writes a new `SoundPreset` row.
- **Color-noise preview** — non-blocking; preview plays at low volume until user releases.

## Screen — 기록 (Records)

Adherence visibility + outcome tracking. Maps to handwritten plan §5.

```
┌─────────────────────────────────────┐
│  기록                               │  ← top bar
│                                     │
│  [ 달력 ] [ 청취 ] [ TFI ]         │  ← segmented tabs
│  ━━━━━━━                            │
│                                     │
│        2026년 5월                    │  ← month header (← → swipe)
│  일 월 화 수 목 금 토              │
│        1  2  3  4 ✓5 ✓6           │  ← ✓ on days that hit
│  ✓7 ✓8 ✓9 10 ✓11 .. .. .. ..       │     daily_listening_goal_min
│  ..  ..  ..  ..  ..  ..  ..        │
│                                     │
│  ┌─ 이번 주 요약 ────────────────┐ │
│  │  일평균 청취  98 분            │ │  ← week summary
│  │  지난 주 대비  +12 분          │ │     pulls from
│  │  연속 청취일   7 일 🔥         │ │     ListeningSession agg
│  └────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
   홈      소리      기록      설정
                     ●
```

When the **청취** segment is selected, the calendar swaps to a chronological list of `ListeningSession` rows (date, duration, preset name, volume). When **TFI** is selected, it swaps to a line chart of TFI total score over time + a list of past `TFIAssessment` rows tappable for per-item review.

### Element placement rationale

| Element | Position | Why here |
|---|---|---|
| Segmented tabs (달력 / 청취 / TFI) | Top of content area | Three distinct lenses on the same data; segmented control is clearer than 3 sub-tabs in the bottom nav. |
| Calendar (default) | Center | The "did I do it" gestalt view. ✓ marks come from a derived view over `ListeningSession` aggregating per-day minutes against `daily_listening_goal_min`. |
| Week summary card | Below calendar | Quantifies what the calendar shows. Streak (🔥) is reserved as a subtle reinforcement; not a Duolingo-style badge wall. |
| Listening log (alt view) | When 청취 selected | Per-session detail for users who want it. Most won't open this often. |
| TFI history (alt view) | When TFI selected | Line chart + list. Tap a point → full per-item view of that assessment. MCID flags (Tier 1+) plug in here later per [[wiki/app/plan]]. |

## Screen — 설정 (Settings)

Meta: re-watch tutorials, retake TFI, adjust cadence and goals, theme, about. Maps to scattered preferences across [[wiki/app/architecture]] DataStore table.

```
┌─────────────────────────────────────┐
│  설정                               │
│                                     │
│  ── 청취 목표 ──                     │
│  일일 청취 목표         120 분  ›   │
│                                     │
│  ── 설문조사 ──                      │
│  TFI 주기              2주마다  ›   │
│  TFI 다시 작성                  ›   │
│  마지막 작성: 2026-04-29            │
│                                     │
│  ── 학습 자료 ──                     │
│  이명 메커니즘 다시 보기        ›   │  → mechanism explainer
│  튜토리얼 다시 보기             ›   │  → onboarding tutorial
│                                     │
│  ── 주파수 ──                        │
│  주파수 다시 측정               ›   │  → matcher
│  현재: 4,250 Hz · 노치              │
│                                     │
│  ── 화면 ──                          │
│  테마                다크 모드  ›   │
│                                     │
│  ── 알림 ──                          │
│  알림 권한                  켜짐   │
│                                     │
│  ── 정보 ──                          │
│  앱 버전                   1.0.0    │
│  임상 면책 조항                 ›   │
│  오픈소스 라이선스              ›   │
│                                     │
└─────────────────────────────────────┘
   홈      소리      기록      설정
                              ●
```

### Element placement rationale

Grouped by purpose, not alphabetical:

- **청취 목표** — daily minutes goal (drives 홈 ring + 기록 ✓ marks)
- **설문조사** — TFI cadence and re-take entry point. Shows last-completed date.
- **학습 자료** — re-viewable mechanism explainer + onboarding tutorial. Required by [[wiki/app/plan]] §1.
- **주파수** — matcher re-entry, with the saved value shown read-only here for context.
- **화면** — theme (dark mode default per plan #2 once it lands).
- **알림** — notification permission status; tapping opens system settings.
- **정보** — version, clinical disclaimer (the line that says "not a clinician replacement" per [[wiki/app/prd]]), OSS licenses.

> **Clinical disclaimer** is required, not optional. Wording derived from [[wiki/app/prd]] "What it ISN'T" section + [[wiki/app/red-flags]].

## Cross-screen patterns

### The play button

Same visual everywhere (orange circle, ~52–64 dp, single-icon ▶ / ■). Located on:

- **Matcher** — plays the tonal pitch at `frequency_hz`
- **홈** — plays the *active preset* (frequency + processing mode + color noise + ambient mix)
- **소리 → 미리듣기** — plays a temporary mix that has not yet been saved as a preset

All three route through `AudioRepository`, which drives `TherapyAudioService` + `AudioEngine`. The visual identity reinforces "this is the same engine." See [[wiki/app/architecture]] §"Audio Foreground Service".

### Preset chip language

When summarizing a `SoundPreset` in a card (e.g. the 홈 active-preset card), use a `·`-separated chip row in this order:

```
{processing mode} · {color noise} · {top ambient layer}
e.g. "노치 · 핑크 · 빗소리"
```

- Always exactly three tokens for visual consistency.
- "Top ambient layer" = the layer with the highest gain. If all are 0, omit the third token (`"노치 · 핑크"`).
- Processing mode tokens: `노치` / `증폭`.
- Color noise tokens: `핑크` / `화이트` / `브라운` / `노이즈 꺼짐`.

### Conditional rendering rules

| Element | Condition | Notes |
|---|---|---|
| 홈 TFI prompt card | `today ≥ last_tfi_date + tfi_cadence_weeks` | Dismissable for the day; returns next day until completed. |
| 홈 streak badge | `current_streak_days ≥ 3` | Below the 3-day threshold, hide entirely — avoid celebrating insignificant streaks. |
| 소리 자연음 미리듣기 | Always available | Preview is independent of `isPlaying` global state. |
| 기록 calendar ✓ | Day's summed listening minutes `≥ daily_listening_goal_min` | Derived view over `ListeningSession`. |

### Korean copy register

- **Polite, calm, not infantilizing.** This is a clinical companion app for adults with a chronic condition. Avoid kiddie language ("잘했어요!"), avoid marketing exclamations, avoid emoji-heavy copy.
- **Verbs in `-하기` / `-요` form**, not `-하세요` for buttons (e.g. `저장하기`, `시작하기` — not `저장하세요`).
- **Generic instrument wording for TFI:** the user sees `이번 주 설문조사`, not `TFI`. The acronym is for internal use and 설정 screen. See [[wiki/app/architecture]] note on assessment instrument.

## Open design questions

These aren't resolved yet — list here so they're surfaced when the relevant screen is built:

- **Matcher equipment vs. tracker home aesthetic.** The matcher uses a cream/skeuomorphic dial; 홈 in this doc leans flat/health-tracker. Worth deciding whether the 홈 ring matches the matcher's dial visual language for cohesion, or stays flat for legibility. _Currently leaning flat — the matcher is a one-time tool, 홈 is daily glance._
- **Sleep-timer placement on 홈.** Chip (this doc) vs. card (more discoverable but takes vertical space). _Currently chip; revisit after first user test._
- **Streak gamification depth.** Per [[wiki/app/plan]] §5, streak is "v1.1." This doc reserves the slot but does not over-design it. Keep it muted — TRT is not Duolingo.
- **Diary entry depth.** Quick severity/stress sliders inline on 홈, or a separate sheet? This doc proposes a sheet to keep 홈 calm; if adherence to diary drops, reconsider inline.
- **Matcher subscreen vs. tab.** Currently the matcher would become a subscreen of 소리. Alternative: keep the matcher as its own root tab during the first 4 weeks of use (when re-measurement is more common), demote later. _Defer until onboarding tutorial work is done._

## Implementation notes (forward references)

When this doc converts to code, the following `ui/*` packages will be created per [[wiki/app/architecture]]:

- `ui/home/HomeScreen.kt` + `HomeViewModel.kt` — new
- `ui/sounds/SoundSettingsScreen.kt` + `SoundSettingsViewModel.kt` — new; absorbs the matcher as a subscreen
- `ui/records/RecordsScreen.kt` + `RecordsViewModel.kt` — new; segmented tabs internally
- `ui/settings/SettingsScreen.kt` — extend existing placeholder
- `ui/onboarding/` — first-launch flow + mechanism explainer (per [[wiki/app/plan]] §1)
- `ui/assessment/TFIQuestionnaireScreen.kt` — TFI flow (per [[wiki/app/plan]] §4)

`MainActivity.kt` `RootScaffold` enum grows from `{ Matcher, Settings }` to `{ Home, Sound, Records, Settings }`, plus a separate `onboarding_complete` gate that bypasses the scaffold entirely on first launch.

No code changes attached to this doc — it is design groundwork. Build order tracked in [[wiki/app/plan]].

## See also
- [[wiki/app/ux-principles]] — design rules synthesized from the two UX sources
- [[wiki/app/prd]] — product scope (what the app is and isn't)
- [[wiki/app/architecture]] — Android codebase + data-model architecture
- [[wiki/app/plan]] — implementation sequence (one active task at a time)
- [[wiki/clinical/trt]] — TRT 12–24 month arc framing
- [[wiki/clinical/sound-therapy]] — sub-mixing-point sound therapy rationale
