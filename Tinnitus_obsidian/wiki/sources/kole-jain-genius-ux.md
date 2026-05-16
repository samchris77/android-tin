---
title: "How to Think Like a GENIUS UI/UX Designer"
type: source
aliases: ["Kole Jain Genius UX", "Genius Designer Framework"]
tags: [app/design, app/ux, source/video]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-12
---

# How to Think Like a GENIUS UI/UX Designer (Kole Jain)

- **Type:** Video / tutorial
- **Year:** Unknown (referenced 2026)
- **Channel:** Kole Jain (YouTube)
- **Population / N:** n/a — design tutorial
- **Intervention:** n/a
- **Comparator:** n/a
- **Primary outcome:** n/a
- **Result:** n/a
- **Risk of bias / quality:** n/a — practitioner design guidance, not research
- **Updates this caused in the wiki:** [[wiki/app/ux-principles]], [[wiki/app/navigation]]

## Key findings

- **User intent as north star:** Design starts with "why is the user here?" not aesthetics. Every screen should have one primary action that answers that question.
- **Respect mental models:** 30 years of internet use has baked-in expectations (top/tab nav, L→R flow, high-contrast CTAs). Don't invent novel patterns for their own sake — save uniqueness for micro-interactions.
- **Design for real content:** Build for edge cases (50-char names, missing images, zero-state lists), not placeholder copy. "Content is the liquid; the design is the container."
- **Progressive disclosure:** Show only what is needed now. Collapse secondary controls; reveal on demand. Maximizes screen real estate and reduces cognitive load.
- **Functional animation only:** Animation earns its place only if it guides the user (scroll-shrink search bars, menu-expand). Decorative animation is a trap.
- **Design systems as architecture:** A shared type scale, spacing grid (8pt), and component library is the difference between a one-off page and a scalable product. Know the rules well enough to break them intentionally.

## Quotes worth preserving

> "The goal isn't to impress other designers with 3D graphics or buttery animations; it's to guide a human being to an action as efficiently as possible."

> "Originality for the sake of originality is often a UX failure."

## My / our take

The "user intent north star" framing is directly applicable to the 홈 screen: the user opens the app for one reason (did I do my therapy today, and can I start?). Progressive disclosure validates the TFI prompt being conditional/collapsible. The design-systems point reinforces a consistent Compose component set early — `@Composable` components need a single source of truth.
