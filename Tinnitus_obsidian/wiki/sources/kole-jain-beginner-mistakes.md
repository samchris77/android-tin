---
title: "7 UI/UX Mistakes That Scream You're a Beginner"
type: source
aliases: ["Kole Jain Beginner Mistakes", "7 UX Mistakes"]
tags: [app/design, app/ux, source/video]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-12
---

# 7 UI/UX Mistakes That Scream You're a Beginner (Kole Jain)

- **Type:** Video / tutorial
- **Year:** Unknown (referenced 2026)
- **Channel:** Kole Jain (YouTube)
- **Population / N:** n/a — design tutorial
- **Intervention:** n/a
- **Comparator:** n/a
- **Primary outcome:** n/a
- **Result:** n/a
- **Risk of bias / quality:** n/a — practitioner critique, not research
- **Updates this caused in the wiki:** [[wiki/app/ux-principles]], [[wiki/app/navigation]]

## Key findings

- **User flow gaps:** Beautiful screens that lack state logic fail immediately. Map every state: empty, error, pressed, success. Paper-prototype before designing.
- **Visual noise:** Harsh shadows (default Figma black) and clashing gradients (blue→green) make UIs look dirty. Use monochromatic gradients and soft gray shadows, or remove entirely.
- **Spacing and alignment:** Packed layouts exhaust the eye. A 2–3 column grid on mobile + consistent vertical rhythm (8pt increments) creates hierarchy without words.
- **Inconsistent components:** Mixed corner radiuses, mismatched button styles signal amateur work. One standard radius value; one master button component reused throughout.
- **Iconography strategy:** Mixing icon libraries (line vs. filled, different stroke weights) creates noise. One library for the whole project; labels on non-standard icons.
- **Redundant elements:** Arrows on swipeable carousels, borders around every card — test: remove it; if the design still works, leave it out.
- **Interactive feedback:** A button with no state change feels dead and triggers double-taps. Every tap needs a state change; saves/completions need a success cue.

## Quotes worth preserving

> "Inconsistency is a dead giveaway of a beginner."

> "Remove a border or an icon. If the design still works and is clear, leave it out."

## My / our take

**State planning** is the highest-leverage item here: every screen needs explicit empty, loading, error, and goal-met states. 홈 already maps 4 state variations (per [[wiki/app/navigation]]), which is good. 소리 and 기록 need the same treatment before implementation. The interactive feedback point applies directly to the play/stop button — color change and ring increment must respond within one frame.
