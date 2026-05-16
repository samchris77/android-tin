---
title: "Tinnitus Handicap Inventory (THI)"
type: assessment
aliases: ["THI", "Tinnitus Handicap Inventory", "이명장애척도", "K-THI"]
tags: [assessment/severity, assessment/handicap, evidence/high]
evidence: high
status: stable
sources: ["[[wiki/sources/neca-2022-trt]]", "[[wiki/sources/hwang-bahng-2018-ktmi]]"]
last_reviewed: 2026-05-12
---

# Tinnitus Handicap Inventory (THI)

## TL;DR

The THI is the **most widely used tinnitus self-report instrument in Korean clinical practice** — 25 items, three response choices (예/가끔/아니요), three subscales, total score 0–100 with named severity bands. Developed by Newman, Jacobson & Spitzer (1996). Strong for diagnostic-stage severity classification; less responsive to incremental treatment change than [[wiki/clinical/tfi]]. The app does NOT use THI as the primary tracking instrument (see standing editorial decision in [[wiki/app/architecture]]), but THI is referenced because it is the comparator instrument in nearly every Korean validation study (including [[wiki/sources/hwang-bahng-2018-ktmi]]) and the NECA HTA ([[wiki/sources/neca-2022-trt]]) accepts it as a canonical outcome.

## What it measures

Tinnitus-related handicap across three subscales:

| Subscale | Items | Construct |
|---|---|---|
| Functional (F) | 11 items | Mental, social, occupational, physical limitation |
| Emotional (E) | 9 items | Affective response (anger, anxiety, frustration) |
| Catastrophic (C) | 5 items | Despair, loss of control, sense that things are dire |

Two versions exist: original alpha version (45 items) and beta version (25 items). **Clinical and research practice uses the beta version**; this is the version validated in Korean.

## Response scale and scoring

- Per item: **예 (4점) / 가끔 (2점) / 아니요 (0점)** — three discrete choices.
- Sum: 0–100.
- **Severity bands** (Korean clinical convention):
  - 0–16: 경미함 (Normal/Slight)
  - 18–36: 약함 (Mild)
  - 38–56: 보통 (Moderate)
  - 58–76: 심함 (Severe)
  - 78–100: 극심함 (Catastrophic)

## Korean adaptation

- A Korean version of the THI (K-THI) is in widespread clinical use and is the comparator in [[wiki/sources/hwang-bahng-2018-ktmi]].
- This wiki does **not** yet have a dedicated K-THI validation source page — the canonical K-THI validation paper has not been ingested. Open task.

## Comparison with related instruments

- **vs [[wiki/clinical/tfi]]** — TFI uses fine-grained 0–10 and 0–100% scales and is designed to be **responsive to treatment change**; THI's three-choice scale is coarser and less sensitive to incremental improvement. The 2018 K-TMI paper (p. 96) explicitly criticizes THI's vocabulary ("종종", "가끔") as ambiguous for measurement purposes.
- **vs [[wiki/clinical/tmi]]** — THI measures perception + reaction, weighted toward reaction (functional/emotional/catastrophic handicap). TMI measures perception only.

## Why the app doesn't use THI as primary

Standing editorial decision (see [[wiki/app/architecture]] and the 2026-05-11 log entry):

1. THI is **less treatment-responsive** than TFI. App users measure themselves repeatedly over a 12–24 month TRT/CBT arc — they need an instrument that detects incremental improvement.
2. THI's 3-choice items are coarse; users showing real progress may stay in the same severity band for months, hurting motivation.
3. The app uses [[wiki/clinical/tfi]] as primary and [[wiki/clinical/tmi]] as high-cadence companion.

THI may still appear in the app:
- As a one-time entry-stage severity classification (because users and clinicians know its bands).
- For users who arrive with a recent THI score from a clinic and want to log it alongside subsequent TFI/TMI tracking.

## Open questions / contested claims

- No K-THI validation source page in this wiki yet — needs a Newman-Korean validation paper ingested.
- Should the app surface THI at all? Arguments for: user familiarity, clinic compatibility. Arguments against: introducing a third instrument adds friction and may dilute the TFI-as-primary message. Decision pending.

## Sources

- [[wiki/sources/hwang-bahng-2018-ktmi]] — uses K-THI as the validation comparator; provides Korean severity bands.
- [[wiki/sources/neca-2022-trt]] — accepts THI as a canonical Korean outcome measure for TRT trials.
