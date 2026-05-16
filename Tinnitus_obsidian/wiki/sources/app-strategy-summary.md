---
title: "이명 치료 방법 종합 정리 및 앱 적용 방안"
type: source
aliases: ["App Strategy Summary", "이명 앱 고려사항 2026"]
tags: [source/synthesis, app/strategy]
evidence: n/a
status: stable
sources: ["[[wiki/sources/lee-2026]]", "[[wiki/sources/walter-2025]]", "[[wiki/sources/bauer-2018]]"]
last_reviewed: 2026-04-28
---

# 이명 치료 방법 종합 정리 및 앱 적용 방안 (App Strategy Summary)

- **Type:** Internally generated review / synthesis document
- **Year:** 2026
- **Population / N:** N/A
- **Intervention:** N/A
- **Comparator:** N/A
- **Primary outcome:** N/A
- **Result:** Synthesis of key papers (Lee 2026, Walter 2025, Okamoto 2010) to define application features.
- **Risk of bias / quality:** Internal product management synthesis mapping clinical sources to an MVP roadmap.
- **Updates this caused in the wiki:**
  - Created [[wiki/app/sound-library]]
  - Created [[wiki/app/cbt-program]]
  - Mapped out [[wiki/app/red-flags]]

## Key findings
Synthesizes the literature into a clear MVP product roadmap with prioritized modules:
- 1st Priority (Must-Have Core):
  - **Directive Counseling Education Module**: Translating the neurophysiological model to patients so that they do not perceive the noise as a threat.
  - **Sound Library (Sound Therapy)**: Background sound generator (white/pink/brown/nature), ensuring mixing points are user-adjustable.
  - **Notched Sound Therapy**: Removing the tinnitus frequency by a 1-octave notch based on Okamoto (PNAS 2010), acting as a secondary mechanism to induce lateral inhibition.
  - **CBT Program**: Structured 8-week or longer CBT regimen based on Kalmeda (Walter 2025).
- 2nd Priority: Relaxation exercises, sleep support, and self-assessment trackers (THI, VAS).
- 3rd Priority: Trigger logging, lifestyle guidance, and medical triage.

## Quotes worth preserving
None — internal synthesis document; no verbatim source text to preserve.

## My / our take
This bridges the gap between raw clinical facts and our product execution. We should use this to firmly structure the `wiki/app/` folder. The emphasis on Notched Sound logic should be closely monitored; Okamoto 2010 has promising results but notched therapy generally carries lower-to-moderate evidence compared to pure CBT/TRT. We must highlight Notched Sound as a "differentiation feature/secondary feature" rather than a guaranteed cure.

**Watchlist flag:** Okamoto 2010 (PNAS) — the notched sound therapy basis — has no ingested source page. No evidence level is assigned. Do not promote notched therapy as a must-have without first creating `wiki/sources/okamoto-2010` with a Quality concerns section.
