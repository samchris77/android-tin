---
title: "Hwang & Bahng (2018) — Korean Adaptation of the Tinnitus Magnitude Index"
type: source
aliases: ["K-TMI validation", "Hwang 2018", "Bahng 2018"]
tags: [source/validation, source/korean, assessment/tmi]
evidence: moderate
status: stable
sources: []
last_reviewed: 2026-05-12
---

# Hwang & Bahng (2018) — A Korean Adaptation of the Tinnitus Magnitude Index

**Citation:** Hwang D-H, Bahng J. *A Korean Adaptation of the Tinnitus Magnitude Index: Validity and Reliability.* Audiology and Speech Research 2018;14(2):90–99. doi:10.21848/asr.2018.14.2.90.

- **Type:** validation study (instrument translation + psychometric evaluation)
- **Year:** 2018
- **Population / N:** 215 tinnitus patients (120 F, 95 M; ages 14–85, mean 51) at Severance ENT Hospital, 2016-01 to 2017-10. 40 normal-hearing, 175 with hearing loss.
- **Intervention:** Korean translation of TMI (forward-backward translated by two bilingual audiologists with US doctorates; author permission obtained by email).
- **Comparator:** Korean Tinnitus Handicap Inventory (THI), beta version (25 items).
- **Primary outcome:** Construct validity (Spearman correlation with THI), internal consistency (Cronbach's α).
- **Result:** Validated. α=0.852 (vs original English α=0.86); moderate correlation with THI total (rs=0.403–0.530, all p<0.001). TMI items correlate most strongly with THI **functional subscale** (rs up to 0.562), least with catastrophe subscale — consistent with TMI's design intent of measuring *perception* not *reaction*.
- **Risk of bias / quality:** Single-center; mostly hearing-loss patients (175/215). No test-retest reliability reported for the Korean version (acknowledged limitation).
- **Updates this caused in the wiki:** Created [[wiki/clinical/tmi]]; influences design of self-report cadence in [[wiki/app/architecture]] and [[wiki/app/plan]].

## Key findings

- **TMI = 3 items only.** Derived from the TFI intrusiveness subscale (2 items kept + 1 TFI-dropped item readded). Measures tinnitus **perception (자각)** explicitly — not reaction.
- Items (validated Korean wording — see Appendix of the paper):
  1. "지난 한 주 동안, 이명이 얼마나 크게 들렸습니까?" 0–10 scale (전혀 크지 않았다 → 매우 크게 들렸다).
  2. "깨어 있는 시간 중에 이명을 인지하는 시간은 몇 % 정도 되었습니까?" 0%–100% in 10% steps.
  3. "지난 한 주 동안, 이명이 얼마나 심각하게 느껴졌습니까?" 100mm VAS (전혀 심각하지 않았다 → 매우 심각하다고 느꼈다).
- Average completion time: ~2 minutes.
- **Optimal weighting for 0–100 scaled sum** (MATLAB optimization minimizing |TMI_sum − THI_sum|):
  - Q1 weight: **0.5856**
  - Q2 weight: **0.0423**
  - Q3 weight: **0.3721**
- **Cronbach's α = 0.852** (3 items); removing Q2 actually *increases* α to 0.883, signaling Q2 is the weakest item (respondents tend to over-pick 100%; 56 subjects chose 100% on Q2 vs 16/18 for Q1/Q3). Authors recommend revising or replacing Q2 in future versions.
- **Correlation pattern**: TMI items all correlate most strongly with THI Q1 ("이명 때문에 집중하기가 어렵습니까?") — a functional/cognitive item. Suggests perception-only instruments still capture functional impact.

## Theoretical framing

Authors build on two models to justify the perception-only approach:
- **Cognitive-Behavioral Mediation Model** (Rudy 1988, applied to tinnitus by Lee 2004): tinnitus magnitude (predictor) → cognition/behavior (mediator) → emotional reaction (response). Same magnitude → different reactions depending on mediator.
- **Adaptation Level Theory** (Helson 1964; Searchfield 2012 for tinnitus): perceived magnitude is not independent of contextual factors (background noise, mood, attention) — interventions like sound therapy can reduce *perceived* magnitude, which then reduces reaction.

Practical claim: measuring perception alone (TMI) can still predict reaction-domain distress, because the two are linked. TMI is therefore complementary to THI/TFI, not a replacement.

## Quotes worth preserving

- "TMI는 THI의 기능을 똑같이 반영하면서 문항 수만 감소된 설문지가 아니라, THI와는 다르게 이명의 자각만을 정량화한 새로운 측면의 설문지이다" (p. 97) — TMI is a *different* perception-only instrument, not a shortened THI.
- "TMI의 경우 질문지에 '종종', '몹시'와 같은 모호한 표현이 없으며 응답도 0~100점(또는 0~10점) 사이 중 정도에 따라 수치를 선택할 수 있으므로 훨씬 측량적이고 객관적인 지표가 될 수 있다" (p. 97) — TMI uses numeric scales not vague frequency words, more measurement-friendly.
- Authors note: a future version should unify the response scale (currently Q1=0–10, Q2=0–100%, Q3=100mm VAS) for consistency.

## Quality concerns

- No test-retest reliability data for the Korean version.
- Single-center, ENT-clinic population — generalizability to community samples unknown.
- Q2 response distribution is skewed (over-selection of 100%), suggesting item ambiguity or emotional inflation.
- Cutoff for "treatment needed" not established for Korean TMI (acknowledged as future work).

## Our take

The K-TMI fits the app naturally as a **short, repeat-friendly daily/weekly check** alongside a longer monthly TFI:

- 3 items, ~2 min completion — low friction for daily mood/symptom logging in the app's records tab (see [[wiki/app/navigation]]).
- Measures perception only — pairs well with TFI/THI which measure reaction, giving us a two-axis tracker (loudness/awareness vs distress/impact).
- **Already validated in Korean** with published item wording (Appendix) — directly usable in the app (academic publication, not the OHSU TFI's "permission required" form).
- Implementation choice: use the validated weights (0.59 / 0.04 / 0.37) to compute a 0–100 summary score; display Q1 and Q3 prominently in trend charts since Q2 has known reliability issues.

Open decision: do we ingest the original Schmidt 2014 TMI paper (in `raw/`?) to anchor the English-language validity? Currently no — the K-TMI paper restates the relevant findings.
