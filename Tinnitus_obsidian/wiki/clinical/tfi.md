---
title: "Tinnitus Functional Index (TFI)"
type: assessment
aliases: ["TFI", "Tinnitus Functional Index", "이명기능지수"]
tags: [assessment/severity, assessment/outcome, evidence/high]
evidence: high
status: stable
sources: ["[[wiki/sources/ohsu-tfi-2012]]"]
last_reviewed: 2026-05-12
---

# Tinnitus Functional Index (TFI)

## TL;DR

The TFI is a 25-item self-report questionnaire that measures tinnitus severity across 8 functional domains. Designed specifically to be **responsive to treatment change** — it is the primary recommended outcome instrument in modern tinnitus trials, including the Korean NECA HTA ([[wiki/sources/neca-2022-trt]]). The app uses the TFI as its primary repeat-measure outcome.

## What it measures

Negative tinnitus impact across 8 subscales:

| Code | Subscale | Items | Construct |
|---|---|---|---|
| I | Intrusive | #1–3 | Unpleasantness, intrusiveness, persistence |
| SC | Sense of Control | #4–6 | Reduced sense of control |
| C | Cognitive | #7–9 | Cognitive interference |
| SL | Sleep | #10–12 | Sleep disturbance |
| A | Auditory | #13–15 | Auditory difficulties attributed to tinnitus |
| R | Relaxation | #16–18 | Interference with relaxation |
| Q | Quality of Life | #19–22 | Quality of life reduced (4 items, not 3) |
| E | Emotional | #23–25 | Emotional distress |

Recall window: "Over the past week."

## Response scales

- **Items #1 and #3**: percentage scale (0%–100% in 10% steps).
- **Items #2, #4–25**: 0–10 numeric rating scale.

## Scoring algorithm

**Overall TFI score (0–100):**
1. For items #1 and #3, divide the percentage value by 10 to get a 0–10 value.
2. Sum all valid item scores.
3. Divide by the number of valid items.
4. Multiply by 10.

**Validity:**
- Overall score requires **at least 19 of 25 items** answered (≥76%). Invalid if ≥7 omitted.
- Subscale score requires ≥2 of 3 items (or ≥3 of 4 for Q). Invalid if >1 omitted in a subscale.

**Subscale scores (0–100, per subscale):** sum valid items in subscale → divide by number of valid items → ×10.

**Critical caveat:** the overall TFI score must NOT be computed by averaging subscale scores — the valid-item denominators may differ.

## Interpretation

- Range: 0 (no tinnitus impact) to 100 (maximal impact).
- Severity bands and Minimum Clinically Important Difference (MCID): commonly cited MCID = 13 points (Meikle 2012); not universally standardized. The wiki should not present band cutoffs without a Korean-validation source — flagged as an open question.
- **Strength vs THI**: TFI is more responsive to within-subject change over time. THI tends to "stick" — useful for diagnosis-stage severity classification but less useful for week-over-week tracking. See [[wiki/clinical/thi]] for comparison.

## Comparison with related instruments

- **vs [[wiki/clinical/thi]]** — THI is shorter to administer (25 items, 3 response choices), longer-established in Korean clinical practice, and severity-banded (normal/mild/moderate/severe/catastrophic). TFI uses finer 0–10 and 0–100% scales, more responsive to change. The two are complementary, not interchangeable.
- **vs [[wiki/clinical/tmi]]** — TMI is a 3-item perception-only short form derived from the TFI intrusiveness subscale. TMI measures perception (자각); TFI measures perception + reaction across all 8 domains.

## Korean adaptation

A Korean-language TFI (K-TFI) exists in the literature but is **not in this wiki yet** — open task. The validated [[wiki/clinical/tmi]] paper ([[wiki/sources/hwang-bahng-2018-ktmi]]) was derived from the Schmidt 2014 TMI, which itself was derived from the TFI development data. No K-TFI source has been ingested into `raw/` to date.

## Licensing / copyright

Copyright © 2008, 2012 Oregon Health & Science University — **"permission required"** is printed on every page of the OHSU form. Verbatim reproduction of item wording in the app UI requires explicit license from OHSU. The scoring algorithm and subscale structure (described above) are facts about the instrument and can be referenced freely.

**App implementation note:** before shipping a TFI questionnaire screen with verbatim item text, the team must (a) obtain OHSU permission, or (b) use a licensed validated Korean translation that already carries permission, or (c) replace with an unlicensed instrument (e.g. [[wiki/clinical/tmi]] which is published with permission in academic literature). See [[wiki/app/prd]] and [[wiki/app/red-flags]] for the app-side risk note.

## App relevance

The app uses TFI as its **primary outcome instrument** for repeated assessment over the TRT/CBT arc. Editorial standing decision: TFI over THI because of treatment-responsiveness. Implementation:

- Cadence: every 4 weeks (configurable via DataStore key `tfi_cadence_weeks`).
- Display: overall 0–100 score as primary KPI; 8 subscales as a small-multiples chart so users can see *which* domain is improving (sleep often improves first; auditory often last).
- MCID flag (≥13 point change) shown to motivate adherence.
- Trend over time charted against listening minutes and CBT module completion.
- See [[wiki/app/architecture]] for DataStore keys (`tfi_cadence_weeks`, `last_tfi_date`) and the planned `assessment/` UI module.

## Open questions / contested claims

- Korean validation: which K-TFI translation is canonical? Ingest a K-TFI validation paper before shipping.
- MCID for Korean population: should we use the 13-point English MCID or wait for Korean data?
- Should the app provide all 25 items every 4 weeks, or alternate full TFI / short TMI to reduce friction?

## Sources

- [[wiki/sources/ohsu-tfi-2012]] — instrument form + scoring instructions
- [[wiki/sources/neca-2022-trt]] — accepts TFI as one of the canonical Korean outcome instruments
