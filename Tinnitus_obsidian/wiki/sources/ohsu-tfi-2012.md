---
title: "Tinnitus Functional Index (TFI) — OHSU Questionnaire Form"
type: source
aliases: ["TFI form", "OHSU TFI", "Meikle TFI"]
tags: [source/instrument, assessment/tfi]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-12
---

# Tinnitus Functional Index (TFI) — OHSU Questionnaire Form

- **Type:** clinical instrument (self-report questionnaire + scoring instructions)
- **Year:** 2008 / 2012 (revised)
- **Copyright holder:** Oregon Health & Science University — "permission required" notice on every page.
- **Author of underlying development paper:** Meikle MB, Henry JA, Griest SE, Stewart BJ, Abrams HB, McArdle R, et al. (2012). *The tinnitus functional index: Development of a new clinical measure for chronic, intrusive tinnitus.* Ear and Hearing, 33(2), 153–176.
- **Population / N:** Validation population — adults with chronic, intrusive tinnitus.
- **Intervention / Comparator:** N/A — instrument paper.
- **Primary outcome:** Self-reported tinnitus severity across 8 domains.
- **Result:** Validated 25-item instrument with treatment-responsiveness superior to THI.
- **Risk of bias / quality:** N/A — instrument source. Wide adoption since 2012 supports validity.
- **Updates this caused in the wiki:** Created [[wiki/clinical/tfi]]; cross-linked from [[wiki/clinical/tinnitus]], [[wiki/clinical/trt]], [[wiki/clinical/cbt]], [[wiki/app/prd]], [[wiki/app/architecture]], [[wiki/app/plan]].

## Key findings (instrument structure)

- 25 items, 8 subscales: I (Intrusive #1–3), SC (Sense of Control #4–6), C (Cognitive #7–9), SL (Sleep #10–12), A (Auditory #13–15), R (Relaxation #16–18), Q (Quality of Life #19–22, 4 items), E (Emotional #23–25).
- Items #1 and #3 use a 0–100% scale (divided by 10 before summing); items #2 and #4–25 use a 0–10 scale.
- **Overall score** = (sum of valid item scores / number of valid items) × 10. Range 0–100.
- **Validity threshold:** at least 19 of 25 items must be answered (≥76%). Overall score is invalid if ≥7 items are omitted.
- **Subscale score** = same formula per subscale; invalid if >1 item omitted in that subscale.
- **Do not** compute the overall score by averaging subscale scores — valid items differ between the two calculations.

## Quotes worth preserving

- "Permission required" (printed on every page of the form). Reproduction of verbatim item wording in app UI requires explicit license from OHSU.

## Quality concerns

None on the instrument itself. Operational concern: **licensing**. Verbatim Korean translation in the app must be backed by an OHSU permission/license. Validated Korean TFI translations exist in academic literature (search for K-TFI validation studies); cite and license the version actually used.

## Our take

The TFI is **the app's primary outcome instrument** — chosen over THI because it was designed specifically for measuring change with treatment (THI is reliable but less responsive to within-subject change over time). See [[wiki/clinical/tfi]] for full structure and Android implementation notes. Editorial standing decision: use TFI even though some Korean sources default to THI.

## Sources page metadata

This source page documents the instrument form itself (the 25 items + scoring instructions). The underlying validation paper (Meikle 2012) is referenced indirectly and would warrant its own source page if a copy is added to `raw/`.
