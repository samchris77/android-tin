---
title: "Transcranial Direct Current Stimulation (tDCS) and tRNS"
type: therapy
aliases: ["tDCS", "tRNS", "transcranial direct current stimulation", "transcranial random noise stimulation", "경두개 직류 자극"]
tags: [therapy/neuromodulation, evidence/low]
evidence: low
status: stable
sources: ["[[wiki/sources/lee-2026]]"]
last_reviewed: 2026-04-28
---

# Transcranial Direct Current Stimulation (tDCS) and tRNS

## TL;DR

tDCS delivers weak constant electrical current through scalp electrodes to modulate neuronal excitability without triggering action potentials — anodal stimulation increases excitability, cathodal decreases it. For tinnitus, the best-evidenced protocol combines bifrontal tDCS (targeting DLPFC) with transcranial random noise stimulation (tRNS) over the left temporal auditory cortex. Evidence is `low` — positive meta-analytic effects exist but studies are small and heterogeneous. Must be combined with TRT or CBT. No FDA approval for tinnitus.

## What it is

**tDCS:** Electrodes (anode + cathode) placed on the scalp deliver 1–2 mA of constant direct current for 10–30 minutes. The current shifts the resting membrane potential:
- **Anodal (anode under):** depolarizes neurons → ↑ excitability
- **Cathodal (cathode under):** hyperpolarizes neurons → ↓ excitability

Unlike [[wiki/clinical/rtms]], tDCS does not trigger action potentials — it modulates the threshold at which neurons fire. Effects outlast the stimulation session (after-effects) via NMDA-dependent plasticity mechanisms.

**tRNS (transcranial random noise stimulation):** Delivers alternating current at random frequencies (typically 0.1–100 Hz). Unlike tDCS, it is polarity-independent (no fixed anode/cathode distinction). Its primary mechanism is **stochastic resonance** — random noise at the correct amplitude enhances signal detection in nonlinear systems and can desynchronize pathological neural oscillations. For tinnitus, tRNS is applied over the left temporal auditory cortex to disrupt thalamocortical dysrhythmia (Δ/θ–γ coupling) at its cortical source.

## Theoretical basis

The combination tDCS + tRNS protocol targets multiple nodes of the [[wiki/clinical/triple-network-model]] simultaneously:
- **Cathodal tDCS to left DLPFC (F3):** Inhibits the overactive distress-attention component of the CEN.
- **Anodal tDCS to right DLPFC (F4):** Strengthens the attention regulation / executive control network.
- **tRNS to left temporal auditory cortex (T3/LTA):** Desynchronizes the thalamocortical dysrhythmia that generates the tinnitus signal itself.

This three-target approach theoretically addresses both the auditory source (tRNS/LTA) and the network that sustains distress and attention (bifrontal tDCS).

## Evidence

**Strength of evidence: Low.**

- **Best protocol (JAMA Otolaryngol HNS 2020 meta-analysis, Chen JJ et al.):**
  - Cathodal F3 + anodal F4 + tRNS T3 combination showed the **greatest improvement in tinnitus severity and quality of life** compared to other tDCS configurations.
- **Temporal target vs. DLPFC (Martins ML et al., Neurophysiol Clin 2022 meta-analysis):**
  - Both effective overall.
  - Left temporal stimulation outperformed DLPFC for loudness and distress outcomes.
- **rTMS vs. tDCS comparison (Heiland LD et al., Otolaryngol HNS 2024, n=1,186):**
  - Short-term: tDCS showed comparable or greater THI improvement than rTMS.
  - Long-term: rTMS showed greater sustained benefit.
  - Depression co-outcomes: tDCS showed larger BDI effect.
- **No FDA approval** for tinnitus. Currently used in research settings in Korea; not in standard clinical routine at most centers.

## Standard clinical protocol (combination)

Per To WT et al. (J Neural Transm 2017) and current Korean clinical experience:
- **tDCS:** 1.5 mA, 20 minutes
  - Cathode at F3 (left DLPFC) — inhibitory
  - Anode at F4 (right DLPFC) — excitatory
- **tRNS:** 2 mA, 0.1–100 Hz (low-frequency band), 20 minutes
  - Applied to T3 (left temporal auditory cortex)
- **Frequency:** 2 sessions/week × 4 weeks = 8 sessions total
- **After-effects:** Optimal window appears to be sessions of 9–13 minutes duration for sustained daily after-effect; beyond ~13 minutes the after-effect can paradoxically decrease (Monte-Silva et al., Brain Stimul 2013).

## tDCS vs. tRNS — key differences

| Feature | tDCS | tRNS |
|---------|------|------|
| Current type | Constant DC | Random AC (noise) |
| Polarity | Anode (↑) / Cathode (↓) | Polarity-independent |
| Cell effect | Membrane potential shift | Na⁺ channel repeated opening → temporal summation |
| Network effect | Local excitability/inhibition balance | Stochastic resonance → desynchronization |
| After-effect | NMDA-dependent long-term plasticity | Immediate network dispersion; less studied long-term |

## Indications / contraindications

**May be indicated:**
- Chronic subjective tinnitus, as adjunct to TRT/CBT.
- Research setting; beginning to be used in some Korean clinical contexts.
- At-home devices exist but evidence for unsupervised use is insufficient.

**Contraindicated:**
- Metallic implants in the head.
- Active skin lesions at electrode sites.
- Pregnancy.
- Seizure history.

**Quality concern — at-home devices:** Commercial at-home tDCS/tRNS devices are available (e.g., Ybrain MINDD STIM in Korea). These are being used outside clinical supervision. Evidence for unsupervised at-home use for tinnitus specifically is insufficient. Apply watchlist scrutiny.

## Common confusions

- **tDCS ≠ ECT, ≠ rTMS.** tDCS uses sub-action-potential current (no seizure, no magnetic field). It is gentler and has a different mechanism from rTMS.
- **Anodal = excitatory, cathodal = inhibitory** — but this is a simplification. At the network level, effects depend on orientation and anatomy. The DLPFC protocol is counterintuitive: cathodal F3 inhibits the left DLPFC distress network while anodal F4 strengthens right DLPFC regulation.
- **tRNS is not just noise.** It exploits stochastic resonance — a specific physical phenomenon where adding the right amount of noise to a nonlinear system enhances its signal-processing capability. In tinnitus, it disrupts pathological synchronization rather than adding random interference.
- **tDCS alone is insufficient.** Must combine with TRT or CBT. Same principle as rTMS — neuromodulation cannot retag the tinnitus signal without behavioral reinforcement.

## App relevance

tDCS is a clinical/research intervention and cannot be delivered via app. The app should:
- Not recommend at-home tDCS devices — evidence is insufficient and unsupervised use raises safety concerns.
- Accurately represent tDCS as an emerging research option, not a proven standalone treatment.
- Patients receiving tDCS in a clinical trial should continue TRT via the app.

## Sources

- [[wiki/sources/lee-2026]]
