---
title: "Gemini chat — Records page & passive-tracking architecture proposal"
type: source
aliases: ["Gemini Records Architecture", "Gemini Passive Tracker"]
tags: [source/design, source/llm-chat, app/records, app/architecture, watchlist/llm-output]
evidence: n/a
status: draft
sources: []
last_reviewed: 2026-05-14
---

# Gemini chat — Records page & passive-tracking architecture (2026)

- **Type:** design proposal (LLM chat output, Gemini)
- **Year:** 2026 (file timestamps 2026-05-13)
- **Source files:** `raw/gemini_chat/gemini-code-1778723974953.md` (architecture writeup), `raw/gemini_chat/gemini-code-1778723980883.kt` (`AudioSessionTracker` reference impl), `raw/gemini_chat/gemini-code-1778723986116.kt` (`LiveSessionPill` Compose reference impl)
- **Population / N:** n/a — design artifact
- **Intervention:** n/a
- **Comparator:** n/a
- **Primary outcome:** n/a
- **Result:** n/a
- **Risk of bias / quality:** **High.** LLM-authored, no citations, no evidence base. Treat as one designer's opinion captured in chat. Code is illustrative, not production.
- **Updates this caused in the wiki:** none yet — divergences from the implemented behavior in [[wiki/app/architecture]] / [[wiki/app/plan]] surfaced to the user for triage before any clinical or product page changes (see "Open questions" below).

## Key findings

The proposal covers three areas:

1. **기록 (Records) tab as a tabbed interface** with two tabs — "Overview & History" (weekly summary dashboard + month tracker calendar + recent-entries list) and "TFI Scores" (line chart + historical list + new-assessment button). A global FAB triggers a "Quick-Log Bottom Sheet" for diary entries.
2. **Passive session tracking** with three explicit rules:
   - **Dominant-sound tally** — if the user switches sounds mid-session, time is tallied per sound and the session is saved under the longest-played one.
   - **60-second pause debounce** — pausing doesn't end the session; a 60 s countdown starts. Resume → seamless continuation. Timeout → finalize.
   - **5-minute minimum** — sessions under 300,000 ms are discarded.
3. **`LiveSessionPill`** — a floating Compose pill at the top of the screen that's visible only when actively tracking or in the 60 s debounce window. Shows `Tracking: MM:SS` or `Paused (MM:SS)`.

The Kotlin in `AudioSessionTracker` is a reference implementation of (2): a `MutableStateFlow<LiveSessionState>` plus `onPlay / onSoundChanged / onPause`, a ticker coroutine for the 1 Hz UI update, and a `debounceJob` that calls `finalizeSession()` after 60 s of no resume.

## Quotes worth preserving

> "When paused, tracking doesn't end immediately. A 60-second countdown begins. If the user resumes play, the session continues seamlessly. If 60 seconds pass, the session is finalized."

> "Sessions under 5 minutes (300,000 ms) are discarded to prevent database clutter from accidental plays."

> "If a user switches sounds during a session, the app tallies the time for each and saves the session under the sound that was played the longest."

## My / our take

Useful prompt-thinking, but it materially conflicts with the already-landed implementation. Specifically, three points of friction:

### Divergence 1 — minimum session length (5 min vs. 5 s)

The current `ListeningSessionRepository.logSession()` filters out sessions under **5 seconds** (see [[wiki/app/plan]] § "✓ 기록 (Records) calendar — heatmap month grid — 2026-05-13"). The Gemini proposal would discard anything under **5 minutes**. For TRT, where 8-hour daily adherence is the target ([[wiki/clinical/sound-therapy]]), a 5-minute floor risks **silently dropping legitimate short therapeutic exposures** (e.g. matcher-driven re-acclimation, brief pre-sleep top-ups). The current 5 s filter exists only to drop accidental tap-then-tap noise, which is the right scope for this filter. **Decision (2026-05-14): raise the floor to 3 min (180 000 ms)** — compromise between the noise filter (5 s) and Gemini's policy filter (5 min). Tracked as [[wiki/app/plan]] Next #1.

### Divergence 2 — pause behavior (60 s debounce vs. clean stop)

The current `AudioRepository` finalizes a session on user-initiated `stop()` and on `AUDIOFOCUS_LOSS`; a `LOSS_TRANSIENT` (e.g. a phone call) does **not** split the session — engine pauses, latch is set, GAIN resumes. There is **no** user-pause concept distinct from stop, because the UI currently exposes only play/stop (no separate pause button).

The Gemini "60 s debounce on pause" is therefore solving a problem the app doesn't yet have. If pause is added later, debounce is reasonable, but the 60 s number is arbitrary and unsourced. **Recommendation: not actionable until a pause UI exists.**

### Divergence 3 — dominant-sound tally vs. presetLabel

The current `ListeningSession` schema is one row per session with a single (currently always-null) `presetLabel` column. The Gemini design would require either (a) sub-session sound rows + an aggregation pass, or (b) carrying `soundDurations: Map<String, Long>` in memory and writing only the winner. (b) is cheap and matches the schema, but loses the per-sound breakdown that any honest weekly-summary card would want. (a) is a schema change.

This question becomes real only once **presets ship** ([[wiki/app/plan]] Next #8 — "Preset label on logged sessions").

**Decision (2026-05-14): go with (a) — sub-session segments.** Do NOT collapse to a single dominant-sound label; preserve per-sound time. A `ListeningSessionSegment` child table (`sessionId, soundId/presetLabel, startedAtEpochMs, durationMs`) splits on every `onSoundChanged`. The per-sound breakdown is what weekly cards and adherence analytics will want, and the dominance label is misleading at the edges (51/49 looks identical to 99/1). Tracked as [[wiki/app/plan]] Next #2.

### Aligned ideas worth keeping

- **`LiveSessionPill` floating indicator** — genuinely useful UX. The current MainActivity has no system-wide "you are still tracking" signal once the user leaves the matcher / 소리 screens. The foreground-service notification covers this in the system tray, but an in-app pill (e.g. above the bottom-nav) would tighten the feedback loop. Worth a Next item.
- **TFI scores as a separate sub-view of 기록** — already aligned with [[wiki/app/plan]] Next #4 ("TFI history trend on 기록"), though the current plan places it as a section on the same screen rather than a tab. The tab-vs-section choice can wait until weekly cards land.
- **Quick-Log Bottom Sheet for diary entries** — the diary entity exists ([[wiki/app/architecture]] § Room entities → `DiaryEntry`) but the entry UI does not. A FAB + bottom-sheet with chip-only inputs (no typing) is the right shape for a daily-friction-minimized capture flow. Worth a Next item once the calendar drilldown ([[wiki/app/plan]] Next #3) lands and we have a place to surface the entries.

## Quality concerns

Watchlist item — this is LLM chat output, not a citation. It has been ingested as a **design proposal source** for traceability, not as evidence. Any claim derived from it must be marked `evidence: n/a` and link back here, and no clinical claim originates from this source.
