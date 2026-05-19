# Wiki Log

## [2026-05-19] feature | App fixes round 2 — 17 polish items shipped ✓
- Foundation: `AudioEngine.Mode.OFF` (filter bypass + zero gain in `generate()`), `AudioRepository` "off" mapping, sleep-timer lift (`SleepTimerState` + `sleepTimer: StateFlow` + `setSleepTimer/cancelSleepTimer`).
- Home: dropped 설정 gear; new `PresetPickerBottomSheet` triggered by hero card preset name tap; sleep-timer copy "${set}분 설정 · N분 남음"; `MiniPlayer.visible = current != Tab.Home`.
- 소리: merged `FrequencyCard` + `ProcessingModeCard` → `FrequencyAndProcessingCard`; master `Switch("주파수 처리 사용")` toggles `PROCESSING_MODE = "off"` with dimmed segments; removed 끄기 pill + 듣기 테스트 button; re-tap selected color → deselect.
- 기록: spillover cells → `Color.Transparent` (was `Heat0`); chevrons `Ink2` → `Teal`; `WeeklySummarySection` call commented out (code retained); `heatColor` now normalizes by `dailyGoalMs` capped 1.0 (was in-month `maxDayMs`).
- 설정: new `ChevronRow` 치료 시작일 with `DatePickerDialog`; disabled `ValueRow` → inline `SegmentedControl` 2/4/6시간; `ActionRow` → `ChevronRow`; `TfiCadenceRow` value wrapped in `Pill(TealSoft)`.
- MiniPlayer: signature added `liveSession`, `sleepTimerRemainingSec`, `onLogSession`; 1 Hz elapsed `MM:SS` ticker; `수면 MM:SS` when sleep active; circular 세션 기록 button (`Icons.Filled.EditNote`) → MainActivity-hosted `QuickLogBottomSheet` pre-filled with active preset.
- MainActivity wired all new Flows + callbacks; `quickLogPrefillPreset` state added.
- Build: `:app:compileDebugKotlin BUILD SUCCESSFUL in 663ms`. Emulator walk-through pending.
- Updated [[wiki/app/plan]] (Now → empty), [[wiki/app/done]] (full write-up appended).

## [2026-05-19] restructure | plan.md split — created done.md archive, promoted Next → Now
- Created [[wiki/app/done]] (404 lines) — moved all 27 ✓ Done entries from plan.md verbatim. Frontmatter `type: app`, `tags: [app/planning, app/archive]`, `status: stable`.
- Rewrote [[wiki/app/plan]] (553 → 105 lines): replaced verbose `## Now` (단일 "설정 polish" task) with promoted App fixes round 2 items, broken into 5 screen-grouped subsections (Home / 소리 / 기록 / 설정 / Player) each rendered as proper Now-level h3 with checkbox tasks. Old 설정 polish task folded into 설정 subsection as item #3. `## Done` now an 8-item headline list pointing to [[wiki/app/done]].
- Updated [[wiki/index]] — added [[wiki/app/done]] under App Strategy & Features.

## [2026-05-19] update | plan.md — App fixes round 2 from user walkthrough
- Added new `## Next` section, item #1 "App fixes round 2 — 2026-05-19 user review" with 16 sub-items grouped by screen (Home / 소리 / 기록 / 설정 / Player).
- Source: two annotated screenshots from 2026-05-19 user review (`/Users/midnight/Screenshots/Screenshot 2026-05-19 at 7.28.{15,25} PM.png`).
- Key directional decisions captured: remove duplicate playback controls from Home (delegate to MiniPlayer + 소리); collapse top three Sound-screen cards into one block; add `PROCESSING_MODE = "off"` enum value; toggle-to-deselect color-noise pattern (remove 끄기 + 듣기 테스트); hide (not delete) 주간 요약; discrete 2h/4h/6h listening-goal selector that propagates to Home gradient + Records heatmap bucketing.
- Updated [[wiki/app/plan]]

## [2026-05-19] update | architecture.md — refresh to reflect shipped state
- `last_reviewed` 2026-05-11 → 2026-05-19; status: draft → stable.
- Folder layout: marked all shipped UI packages ✓ (home, sounds, records, assessment, onboarding, components, theme/components, theme/Tokens, theme/PressEffect). Removed "(stub)" notes from home/sounds/records.
- Room entities: TFIAssessment, ListeningSession, ListeningSessionSegment, SoundPreset all moved from "planned" to shipped (v5).
- DataStore: 5 keys promoted from planned to implemented (`active_preset_id`, `daily_listening_goal_min`, `treatment_start_date`, `tfi_cadence_weeks`, `last_tfi_date`).
- Audio FGS section: added pause/resume actions and the new pause behavior.
- New Design system section documenting Tokens.kt (Spacing/Radius/Elevation), component library, PressEffect.
- Removed stale `project_android.md` auto-memory pointer from MEMORY.md and deleted the file (user direction).

## [2026-05-16] fix | MVP Bug Fix + Emulator Verification ✓
- **Compile errors fixed (3):** `SurfaceColor` → `Surface` (7 sites, `HomeScreen.kt`); smart-cast on `sleepTimerRemaining` → local `val`; `audioRepo.play()` → `audioRepo.togglePlay()` (`HomeViewModel` + `SoundSettingsViewModel` — `play()` doesn't exist on `AudioRepository`).
- **Streak counter removed** (user direction): `consecutiveDays` field dropped from `HomeUiState`; `🔥 N일 연속` badge removed from `HomeScreen`; treatment-week label kept.
- **Emulator walkthrough passed**: sleep timer sheet, quick log sheet, records calendar + weekly summaries, EntryOptionsBottomSheet — all confirmed working on emulator API 36.1.
- Modified: `ui/home/HomeViewModel.kt`, `ui/home/HomeScreen.kt`, `ui/sounds/SoundSettingsViewModel.kt`
- Updated [[wiki/app/plan]] (added Done entry, cleaned up Now/Next duplicate)

## [2026-05-16] feature | MVP Completion (홈, 소리, 기록 Polish) ✓
- Executed the final wiring and UI integration to complete the MVP, fully applying the "Player-First" Light Theme redesign.
- **Home Screen Wiring**: Implemented `HomeViewModel` to aggregate `todayListenMs`, `dailyGoalMin`, and `treatmentWeek`. Fully replaced `HomeScreen.kt` with the high-fidelity Compose layout based on `home_prototype.html`. Features circular progress play button, live adherence card, Sleep Timer bottom sheet (15/30/60/90 mins), and Quick Log integration.
- **Audio Engine & Sound Settings**: Loaded `brook.mp3` and `fireplace.mp3` into `AudioEngine` and exposed them as sliders in the Ambient Mix card. Implemented `saveAsNewPreset` functionality via a "새 프리셋 저장" dialog on the "+ 저장" button. Wired the "미리듣기" (Preview) button to toggle `AudioEngine` playback without committing to the database.
- **Records Polish**: Added `deleteSession` and `deleteDiary` methods to `RecordsViewModel`. Implemented `EntryOptionsBottomSheet` allowing users to delete individual log entries from the Recent Entries list.
- Updated `PreferenceKeys.kt` and `UserSettingsRepository.kt` with `TREATMENT_START_DATE` and `DAILY_LISTENING_GOAL_MIN`.
- Updated [[wiki/app/plan]] (Now/Next cleared, added MVP Completion to Done)

## [2026-05-16] lint | weekly
- Moved `wiki/app/ect/` files to `wiki/app/` (schema fix — ect/ subfolder violated flat-app rule): [[wiki/app/android-snapshot]], [[wiki/app/cbt-program]], [[wiki/app/red-flags]], [[wiki/app/sound-library]]
- Fixed all bare wikilinks across: [[wiki/index]], [[wiki/clinical/trt]], [[wiki/clinical/cbt]], [[wiki/app/architecture]], [[wiki/app/prd]], [[wiki/app/plan]], [[wiki/sources/neca-2022-trt]], [[wiki/sources/walter-2025]], [[wiki/sources/app-strategy-summary]]
- Added missing "Open questions / contested claims" and "Sources" sections to [[wiki/clinical/cbt]]
- Downgraded [[wiki/sources/walter-2025]] risk of bias from "Very strong" to "Moderate"; added bias rationale; updated `last_reviewed`
- Added "Quotes worth preserving" (N/A) and Okamoto watchlist flag to [[wiki/sources/app-strategy-summary]]
- Remaining open: `wiki/clinical/hyperacusis` (5 refs, no page), `wiki/clinical/lateral-inhibition` (2 refs, no page), `wiki/sources/okamoto-2010` (no source page), NEJM 2018 CBT guideline (no source page)

## [2026-05-13] ingest | Sajid — The Easy Way to Design Top Tier Websites (YouTube)
- Created [[wiki/sources/sajid-top-tier-web-design]] — practitioner web-design guide; overlaps with Kole Jain but adds explicit multiples-of-4 spacing scale, de-emphasis as hierarchy tool, zoom-out test, whitespace-tightening method, incubation step
- Updated [[wiki/app/ux-principles]] — §5 spacing rule promoted from 8 dp base to explicit 4 dp grid (4, 8, 12, 16, 20…) with usage guidance; added new §7 "Hierarchy via de-emphasis, and the zoom-out test"; renumbered "No redundant elements" → §8; added whitespace-bias bullet; added source link
- Updated [[wiki/index]] — added new source at top of Sources list
- Updated [[wiki/tasks]] — checked off this ingest

## [2026-05-12] ingest | TFI form (OHSU) + NECA 2022 TRT HTA + K-TMI validation (Hwang & Bahng 2018)
- Created [[wiki/sources/ohsu-tfi-2012]] — TFI questionnaire form + scoring instructions (2008/2012, OHSU; permission required)
- Created [[wiki/sources/neca-2022-trt]] — Korean government HTA of TRT (NECA-R-21-001-12); Conditionally Recommended; HIGH safety; TRT vs masking SMD −0.74; vs counseling/CBT equivalent
- Created [[wiki/sources/hwang-bahng-2018-ktmi]] — Korean validation of the 3-item Tinnitus Magnitude Index (α=0.852); validated weights Q1=0.59 / Q2=0.04 / Q3=0.37
- Created [[wiki/clinical/tfi]] — assessment page: 25 items, 8 subscales, scoring algorithm; app's primary outcome instrument
- Created [[wiki/clinical/thi]] — assessment page: 25 items, 3 subscales, Korean severity bands; not app's primary but referenced as comparator
- Created [[wiki/clinical/tmi]] — assessment page: 3-item K-TMI with validated Korean wording, weights, and α-removal behavior; app's high-cadence companion to TFI
- Updated [[wiki/clinical/trt]] — added NECA findings section (vs masking, vs counseling, vs CBT, vs no-treatment effect sizes), Korean reimbursement (비급여, 소-4 MZ004), Korean-market devices (Starkey TM-R / J-TRT / Ti-Masker / etc.); fixed THI/TFI references to wikilinks
- Updated [[wiki/clinical/sound-therapy]] — NECA confirms sub-masking sound therapy significantly outperforms masking
- Updated [[wiki/clinical/cbt]] — NECA cross-finding: CBT statistically equivalent to TRT on disability scores, supports CBT-forward app strategy
- Updated [[wiki/clinical/habituation]] — NECA endorses Jastreboff neurophysiological model
- Updated [[wiki/clinical/tinnitus]] — Assessment section now wikilinks to the three assessment pages instead of orphan `assessments/` paths
- Updated [[wiki/index]] — new "Assessments" section; 3 new sources at top of Sources list
- Updated [[wiki/tasks]] — checked off TFI/TMI/THI ingest; new backlog items for K-TFI validation source and OHSU permission

## [2026-05-12] ingest | Kole Jain — Genius UX Designer + Beginner Mistakes (UI/UX videos)
- Created [[wiki/sources/kole-jain-genius-ux]]
- Created [[wiki/sources/kole-jain-beginner-mistakes]]
- Created [[wiki/app/ux-principles]] — synthesizes both into app-specific design rules
- Updated [[wiki/app/navigation]] — added sources frontmatter + ux-principles link
- Updated [[wiki/index]] — 2 new sources + ux-principles app page

## [2026-05-11] feature | Onboarding tutorial shipped
- 7-page Compose onboarding (HorizontalPager) gating first launch; copy synthesized from `wiki/clinical/tinnitus`, `central-gain`, `habituation`, `triple-network-model`, `trt`, `sound-therapy`, `cbt`. Each page composable comments its wiki source for traceability.
- Wired `PreferenceKeys.ONBOARDING_COMPLETE` (previously unread). Re-launch entry from Settings as "튜토리얼 다시 보기".
- Verified on emulator-5554: fresh install → page 1; 시작하기 persists flag; relaunch skips onboarding; replay-from-Settings works; Skip works.
- Updated [[wiki/app/plan]] (Now → Done), [[wiki/app/architecture]] (`ui/onboarding/` ✓, `onboarding_complete` now actively gating).
- Deferred: red-flag screener (separate later task); module guide-throughs for sound-settings / sleep-timer (those modules don't exist yet).

## [2026-05-11] create | App navigation & screen layouts
- Created [[wiki/app/navigation]] — 4-tab bottom nav (홈 / 소리 / 기록 / 설정) + per-screen ASCII layouts + element-placement rationale + cross-screen patterns (play button, preset chip language) + open design questions
- Updated [[wiki/index]] to list the new page under "App Strategy & Features"
- Rationale: current 2-tab nav (`톤 찾기` / `설정`) optimizes for one-time pitch matching, but the daily-use mode for a 12–24 month TRT arc needs a sound-therapy session home. Doc is design groundwork only; no code changes.

## [2026-05-11] update | Plan revised — frequency page extended, Room DB demoted to Next
- Re-opened [[wiki/app/plan]] "Now": frequency page needs notch/amplify mode selector + seamless therapy handoff (per `raw/Plan(수기).md` §3.2)
- Demoted "Wire up Room database" to Next #1
- Done entry for frequency page marked as "base" with pointer to Now additions
- Source: `raw/Plan(수기).md`

## [2026-05-11] update | Android snapshot + architecture aligned with actual code
- Read all 11 Kotlin source files in `com.tinnitustracker`
- Rewrote [[wiki/app/ect/android-snapshot]]: reflects actual 2-tab app (Matcher + Settings), AudioEngine MASK/NOTCH modes, FrequencyMatchingScreen full feature set, placeholder SettingsScreen, no Room yet, no foreground service yet
- Updated [[wiki/app/architecture]]:
    - Corrected package name `com.example.tinnitus` → `com.tinnitustracker`
    - Folder layout now marks ✓ (built) vs. "planned" per actual file tree
    - DataStore keys split into implemented (3) vs. planned
    - Audio foreground service section rewritten to describe current Activity-bound state and planned service
    - `last_reviewed` bumped to 2026-05-11

## [2026-04-28] ingest | Several new sources and App restructuring
- Created [[wiki/sources/bauer-2018]], [[wiki/sources/walter-2025]], [[wiki/sources/app-strategy-summary]]
- Created [[wiki/clinical/cbt]]
- Created [[red-flags]], [[sound-library]], [[cbt-program]]
- Updated [[wiki/clinical/tinnitus]]
- Created [[wiki/index]] and [[wiki/log]]

## [2026-04-28] ingest | Android App Architecture and Task List
- Created [[android-snapshot]], [[wiki/app/app-work-list]], [[wiki/tasks]]
- Updated [[wiki/index]], [[claude]]
- Cleaned up empty directories in wiki/ and raw/

## [2026-05-07] restructure | App project-control docs (PRD / architecture / plan)
- Created [[wiki/app/prd]] — product scope, what it IS / ISN'T, Android-only / Korea-only / free
- Created [[wiki/app/architecture]] — Kotlin package layout, MVVM + Repository, Room entities, DataStore keys, foreground audio service
- Created [[wiki/app/plan]] — single-active-task plan (Now / Next / Later); first task: tinnitus frequency selecting page
- Deprecated [[wiki/app/app_guideline]] and [[wiki/app/app-work-list]] (banner added; content distilled into new files)
- Updated [[wiki/index]]

## [2026-05-11] implement | TherapyAudioService (foreground service) ✓
- **Pattern chosen** (Option A over canonical bound + started): Application-singleton `AudioEngine` + started-only FGS. Rationale documented in `~/.claude/plans/luminous-baking-rocket.md` — boils down to: `AudioEngine` has no Context-dependent state, ViewModel StateFlow exposures need a stable non-null engine, and we have no IPC requirement. Option B would force `flatMapLatest<AudioEngine?>` and downgrade StateFlows to Flows-with-initial-values throughout.
- New files:
    - `TinnitusTrackerApp.kt` — `Application` subclass; `audioEngine: AudioEngine by lazy`
    - `audio/service/TherapyAudioService.kt` — `Service`, `onBind = null`, two actions (ACTION_START → `startForeground`; ACTION_STOP → engine.stop + `stopForeground(REMOVE)` + `stopSelf`)
    - `audio/service/AudioNotification.kt` — channel `therapy_audio` (LOW importance, silent, no badge), NOTIF_ID 1001, single 정지 action, content PendingIntent → `MainActivity` (singleTop+clear-top)
    - `data/repository/AudioRepository.kt` — drop-in replacement for direct `AudioEngine` usage in ViewModel; forwards StateFlows verbatim; `start()` → `ContextCompat.startForegroundService` + `engine.start`; `stop()` → `engine.stop` + `context.stopService`; `togglePlay()`
    - `res/drawable/ic_notification_wave.xml` — vector small icon
- Manifest changes:
    - Permissions: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`
    - `<application android:name=".TinnitusTrackerApp">`
    - `<service ... android:foregroundServiceType="mediaPlayback" android:exported="false">`
    - `MainActivity android:launchMode="singleTop"` (so notification tap doesn't pile up tasks)
- Modified files:
    - `MainActivity.kt` — dropped `private lateinit var audioEngine`; no more `onPause stop()` / `onDestroy release()` (engine outlives the Activity); instantiates `AudioRepository(applicationContext, app.audioEngine)` and passes to factory
    - `ui/matcher/FrequencyMatchingViewModel.kt` + factory — type swap `AudioEngine → AudioRepository`; mass rename `engine → audio`; `togglePlay()` delegates to `audio.togglePlay()`
- **Verification on emulator-5554:**
    - `:app:compileDebugKotlin` green; `:app:assembleDebug` green
    - Install + launch → no crash; `RoomScaffold` logs `id=2 dbFile=...` (Room still works after Application class addition)
    - Tap 재생 → `dumpsys activity services`: `isForeground=true types=0x2 (mediaPlayback) foregroundId=1001 channel=therapy_audio flags=ONGOING|FOREGROUND_SERVICE|ONLY_ALERT_ONCE|SILENT actions=1`
    - HOME → service still `isForeground=true`, process alive, notification persists
    - Tap 정지 (in-app) → service torn down; `cmd notification list` shows nothing for the package
    - No FATAL / AndroidRuntime
- Updated [[wiki/app/plan]] (TherapyAudioService → Done), [[wiki/app/architecture]] (folder layout + Foreground Service section reflects landed state)

## [2026-05-11] implement | Room database scaffold ✓
- Gradle: applied `com.google.devtools.ksp` plugin in `app/build.gradle.kts`; added Room 2.6.1 (`room-runtime`, `room-ktx`, KSP `room-compiler`); `ksp { arg("room.schemaLocation", ...) }`
- New code under `data/database/`:
    - `AppDatabase.kt` — singleton, `@Database(entities=[DiaryEntry], version=1, exportSchema=true)`, `diaryDao()`
    - `entities/DiaryEntry.kt` — id (auto), date (epoch ms), severity, stressLevel, note
    - `dao/DiaryDao.kt` — upsert / observeRange (Flow<List>) / latest / deleteById
- New code under `data/repository/`: `DiaryRepository.kt` (thin DAO wrapper)
- `MainActivity` instantiates `DiaryRepository` next to `UserSettingsRepository`; runs a non-residual round-trip sanity check under tag `RoomScaffold` on every cold start
- Schema exported: `app/schemas/com.tinnitustracker.data.database.AppDatabase/1.json`
- `:app:assembleDebug` green (KSP + compile + APK)
- Updated [[wiki/app/plan]]: Now sub-tasks all `[x]`; updated [[wiki/app/architecture]]: folder layout marks database/✓, repository/DiaryRepository ✓; added `processing_mode` to implemented DataStore keys (previously missed during the frequency-page-extension log entry)

## [2026-05-11] implement | Frequency page extensions (notch/amplify selector + seamless handoff) ✓
- `data/preferences/PreferenceKeys.kt` — added `PROCESSING_MODE` string key (`"notch"` / `"amplify"`)
- `data/repository/UserSettingsRepository.kt` — `processingMode: Flow<String>` + `saveProcessingMode`
- `ui/matcher/FrequencyMatchingViewModel.kt` — `hasTonalTinnitus` & `processingMode` (StateFlow<AudioEngine.Mode>) + `setProcessingMode`; `confirmPitch()` now also calls `engine.setMode(processingMode.value)`
- `ui/matcher/FrequencyMatchingScreen.kt` — new `ProcessingModeCard` shown via `AnimatedVisibility(hasTonalTinnitus == true)`; reuses `OctaveButton` for segmented look
- Both `compileDebugKotlin` builds successful
- Updated [[wiki/app/plan]]: Now is now "Wire up Room database" (promoted from Next #1); frequency-page entry consolidated in Done

## [2026-05-11] revise | App architecture & plan aligned with handwritten basic-functions plan
- Source: `raw/Plan(수기).md` — basic functions: tutorial + tinnitus explainer, sound settings (notch/amplify, color noise, ambient mix, presets), TFI questionnaire, records page (calendar + listening log + weekly averages + assessment history + streaks)
- Updated [[wiki/app/architecture]]:
    - Audio engine: notch _or_ amplify, procedural color noise, ambient mixer (rain/water/thunder)
    - Folder layout: added `ui/sounds/`, `ui/assessment/`, `ui/records/`
    - Data model: added `SoundPreset`, `ListeningSession`; regrouped entities into MVP vs Tier-1+
    - DataStore: added `processing_mode`, `active_preset_id`, `daily_listening_goal_min`, `current_streak_days`, `tfi_cadence_weeks`, `last_tfi_date`, `tinnitus_explainer_seen`
- Updated [[wiki/app/plan]]:
    - Next expanded with MVP items 5–7 (sound settings expansion, TFI questionnaire, records page)
    - Onboarding item rewritten to include tinnitus-mechanism explainer (re-viewable from Settings)
    - Tier 1 TFI bullet rescoped from "basic capture" to "trend lines + MCID flags"
- **Editorial decision**: handwritten plan specifies THI; we use TFI (treatment-responsive) instead — flagged inline in both docs

## [2026-05-07] restructure | Wiki simplified to two-tier (clinical / sources / app)
- Collapsed `conditions/` + `therapies/` + `concepts/` into single `clinical/` folder (`type` frontmatter disambiguates)
- Flattened `app/`: removed `app/features/`, `app/modules/`, `app/safety/` subfolders
- Renamed sources for brevity: `bauer-nejm-2018` → `bauer-2018`, `lee-2026-tinnitus-staff-lecture` → `lee-2026`, `walter-jmir-2025` → `walter-2025`
- Renamed `wiki/app/android-app` → `wiki/app/android-snapshot`
- Hard-deleted empty `pulsatile-tinnitus.md` and the two deprecated docs (`app_guideline.md`, `app-work-list.md`)
- Bulk-rewrote all wikilinks across the wiki + `claude.md` to new paths (and normalized non-prefixed `[[therapies/X]]` style to `[[wiki/clinical/X]]`)
- Updated [[claude]] §2 (directory layout) and §7 (flat `app/`) to reflect the new structure
- Updated [[wiki/index]]

## [2026-05-14] ingest | Gemini chat — Records page & passive-tracking architecture proposal
- Created [[wiki/sources/gemini-records-architecture-2026-05]] (LLM-chat design proposal; watchlist/llm-output)
- Updated [[wiki/index]] (sources section)
- Updated [[wiki/tasks]] — logged ingest + queued two triage items (LiveSessionPill, Quick-Log Bottom Sheet)
- **Did NOT modify** [[wiki/app/architecture]] or [[wiki/app/plan]] — three divergences surfaced for user triage before any change:
  - min-session length: implemented 5 s vs proposal 5 min (recommend keep 5 s for TRT short-exposure honesty)
  - pause behavior: implemented clean stop + focus-loss latch vs proposal 60 s debounce (not actionable until a pause UI exists)
  - dominant-sound tally: not actionable until presets ship ([[wiki/app/plan]] Next #6)

## [2026-05-14] decision | Gemini ingest triage — min-session + per-sound tracking
- **Min session length:** raise 5 s → **3 min (180 000 ms)**. Compromise between 5-s noise filter and Gemini's 5-min policy filter. Added as [[wiki/app/plan]] Next #1.
- **Per-sound time tracking:** add `ListeningSessionSegment` child table; do NOT collapse to a single dominant-sound label. Added as [[wiki/app/plan]] Next #2 (depends on preset rollout, Next #8).
- **60-s pause debounce:** declined for now — no pause UI exists; revisit if pause is introduced.
- Updated [[wiki/app/plan]] (Next list renumbered, two new items at the top, cross-refs fixed), [[wiki/app/architecture]] (ListeningSession row + new Segment row + minimum-floor note), [[wiki/sources/gemini-records-architecture-2026-05]] (decisions captured in-place), [[wiki/tasks]] (triage item closed).

## [2026-05-14] update | plan.md — added gemini layout items to Next queue
- Added Next #5: 기록 tab split (Overview & History | TFI Scores) + Recent Entries List
- Added Next #6: LiveSessionPill floating in-app tracking indicator
- Added Next #7: FAB + Quick-Log Bottom Sheet for DiaryEntry
- Renumbered old #5–#9 → #8–#12; fixed cross-refs (TFI distress analytics → Next #10, per-sound segments → depends on Next #11)
- Updated [[wiki/app/plan]]
