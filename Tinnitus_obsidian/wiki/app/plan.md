---
title: "Implementation Plan"
type: app
aliases: ["Plan", "Roadmap", "Work Plan"]
tags: [app/planning]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-19
---

# Implementation Plan

**One active block at a time.** When `## Now` items ship, the top of `## Later` is promoted. Full write-ups of completed work live in [[wiki/app/done]].

---

## Now

_Empty — App fixes round 2 landed 2026-05-19. See `## Done` ↓ and [[wiki/app/done]] for write-up._

Next up: promote from `## Later` Tier 1 when ready.

---

## Later (backlog, unordered within tier)

### Tier 1 — clinical correctness

- [ ] Mixing-point calibration tool (locks therapy output 3–6 dB below the mixing point) → [[wiki/clinical/sound-therapy]]
- [ ] TRT-phase UI (Orientation → Active Habituation → Consolidation) → [[wiki/clinical/trt]]
- [ ] Continuous red-flag safety screener (30-day cadence) → [[wiki/app/red-flags]]
- [ ] 8-hour sound-therapy adherence engine + occluding-earphone warning → [[wiki/clinical/sound-therapy]]
- [ ] TFI distress-tracking analytics — trend lines, MCID flags → [[wiki/clinical/habituation]]

### Tier 2 — mechanism-targeted

- [ ] Streak / goal-progress badge
- [ ] Spike-mode vicious-cycle interrupter (4-7-8 breath + reframe + sound bump)
- [ ] Salience-retraining daily practice → [[wiki/clinical/triple-network-model]]
- [ ] Somatic modulation assessment + exercise library

### Tier 3 — personalization & education

- [ ] TRT category routing (0–4) at onboarding
- [ ] 9-week TRT directive-counseling curriculum (Korean)
- [ ] Clinician-readable monthly PDF report
- [ ] Honest neuromodulation decision aid (rTMS / tDCS / VNS / 알파-스팀) → [[wiki/clinical/rtms]], [[wiki/clinical/tdcs]], [[wiki/clinical/vns-auricular]]
- [ ] Material You dark-mode opt-in

### Tier 4 — experimental

- [ ] Pitch-matched notched sound therapy → [[wiki/clinical/tonotopic-reorganization]]

---

## Done

Full write-ups archived in [[wiki/app/done]]. Most recent:

- ✓ App fixes round 2 — 2026-05-19 (Home / 소리 / 기록 / 설정 / MiniPlayer polish, 17 sub-items)
- ✓ MVP Bug Fix + Emulator Verification — 2026-05-16
- ✓ MVP Completion (홈, 소리, 기록 Polish) — 2026-05-16
- ✓ 주간 요약 (Weekly summary) cards on 기록 — 2026-05-16
- ✓ State + interactive-feedback audit — 2026-05-16
- ✓ Design tokens + shared component library — 2026-05-14
- ✓ Day-tap drilldown on 기록 calendar — 2026-05-14
- ✓ TFI history trend on 기록 — 2026-05-14
- ✓ Global Mini Player & True Pause — 2026-05-14

---

## See also

- [[wiki/app/prd]] — product scope
- [[wiki/app/architecture]] — code structure
- [[wiki/app/done]] — full implementation history
- [[wiki/tasks]] — wiki maintenance tasks
