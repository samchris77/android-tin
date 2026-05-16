---
title: "UX Principles for the Tinnitus Companion App"
type: app
aliases: ["UX Principles", "Design Principles", "UI Guidelines"]
tags: [app/design, app/ux, app/strategy]
evidence: n/a
status: draft
sources: ["[[wiki/sources/kole-jain-genius-ux]]", "[[wiki/sources/kole-jain-beginner-mistakes]]", "[[wiki/sources/sajid-top-tier-web-design]]"]
last_reviewed: 2026-05-13
---

# UX Principles for the Tinnitus Companion App

Design rules from [[wiki/sources/kole-jain-genius-ux]], [[wiki/sources/kole-jain-beginner-mistakes]], and [[wiki/sources/sajid-top-tier-web-design]], applied to this app. Each rule is grounded in the constraints of a clinical companion for Korean-speaking adults with chronic tinnitus.

## 1. North-star intent per screen

Before designing any screen, answer: *why is the user here right now?*

| Screen | North-star intent |
|---|---|
| 홈 | "Did I do my therapy today? Start a session." |
| 소리 | "Adjust what I'm listening to, or set up a new preset." |
| 기록 | "How am I tracking across the week / month?" |
| 설정 | "Change a preference or re-access a tutorial." |

Every element that doesn't serve that intent should be secondary, conditional, or absent.

## 2. Respect existing mental models

Korean users have the same baked-in smartphone expectations as any user: bottom tab nav, top app bar for context + actions, cards for grouped content, full-screen sheets for contextual flows. TRT is already cognitively demanding — the UI must require zero learning overhead.

The 4-tab bottom nav (홈 / 소리 / 기록 / 설정) maps directly to standard Material patterns. Stick to it.

## 3. Progressive disclosure for clinical complexity

TRT involves a lot of information the user does not need simultaneously:

- **홈** shows the listening ring + play button. TFI prompt only when due. Diary as a row link, not inline sliders.
- **소리** hides the full matcher behind a "다시 측정" link. Preset editing is a scrollable card stack, not a tabbed deep-dive.
- **설정** exposes the mechanism explainer and tutorial as re-viewable links, not persistent banners.

Onboarding flows and questionnaires are *interruptions*, not tabs. They arrive when needed and exit cleanly.

## 4. Design for real content and edge cases

Korean text can run longer than English on longer terms. Every card, chip, and row must handle:

- Long preset names (e.g. "저녁 휴식을 위한 저음 강조 빗소리 설정")
- Zero-state screens (no sessions, no TFI data, no saved presets)
- Fresh install (null `matched_frequency_hz` — 소리 must handle gracefully)
- Goal-exceeded state (홈 ring > 100%)
- TFI overdue by multiple cycles

**Rule:** for every card or list, define the empty state before implementation.

## 5. Consistent components, single source of truth

In Jetpack Compose, every `@Composable` appearing in more than one place is extracted into a shared component:

- **Play/stop button** — one `PolishedPlayButton(variant, size)` lives in `ui/theme/PressEffect.kt`. Variants: `Coral` (홈 hero, cream cards), `Orange` (Matcher dark studio), `Teal` (TFI prompt + secondary CTAs). Do not roll a new gradient + shadow ad hoc — extend the enum.
- **Corner radius** — **16 dp cards, 8 dp chips / cells, 12 dp buttons, 100 dp pills.** Updated 2026-05-14 from the previous "12 dp cards" rule after the design-overhaul audit found drift (10 / 12 / 16 / 22 dp scattered across screens). Wireframe HTML at `TinnitusTrackerAndroid/docs/design/app-wireframes.html` uses `--r-card: 20px`; we honor it loosely — Android density math + Material expectation lands at **16 dp**. Use `Radius.card` / `Radius.chip` / `Radius.button` / `Radius.pill` from `ui/theme/Tokens.kt`, never raw `.dp` literals.
- **Icon library** — `Icons.Filled.*` throughout. Do not mix in `Icons.Outlined.*` variants. No text glyphs (`›`, `▾`, `▶`) where an icon (`ChevronRight`, `ArrowDropDown`, `PlayArrow`) exists.
- **Spacing** — 4 dp grid via `Spacing.xs/sm/md/lg/xl/xxl/section = 4/8/12/16/20/24/32`. Defaults: screen edges 16 dp, card interiors 16 dp, internal gaps 8 dp. Reserve 4 dp only for tight contexts (icon-to-label inside chips). Forbidden values after the 2026-05-14 overhaul: 5, 6, 9, 10, 14, 18, 22, 26, 28 dp. Source: [[wiki/sources/sajid-top-tier-web-design]] (multiples-of-4 framing).
- **Components live in `ui/theme/components/`** as of 2026-05-14: `AppCard`, `PrimaryButton`, `SecondaryButton`, `Pill`, `TagChip`, `SegmentedControl`, `IconBadge`, `ChevronRow`, `EmptyStateCard`, `SectionHeader`. Any screen-level composable that needs a card, button, chip, pill, list row, or empty state pulls from there. Adding a new variant means extending an enum, not building a parallel component.
- **Whitespace bias.** Start every layout overgenerous on spacing, then tighten — not the reverse. Spacing that feels right zoomed into one element looks cramped at full-screen scale.

## 6. Every interactive element needs state coverage

Before implementing any tappable element, define: default, pressed, disabled (if applicable), success/completion (if applicable).

- Play button: pressed → ripple; playing → icon swap to ■.
- TFI submit: tapped → button grays out + progress; success → return to 홈 with quiet confirmation.
- Diary save: immediate visual confirmation on the row link.

## 7. Hierarchy via de-emphasis, and the zoom-out test

From [[wiki/sources/sajid-top-tier-web-design]]:

- **Lower the contrast of secondary copy rather than enlarging the primary element.** On 홈, the listening ring is already large — push it forward by graying down the TFI status row and diary timestamp, not by making the ring even bigger.
- **Zoom-out test** before shipping a screen: scale Android Studio's preview to 25–33 % (or step back from the device). If the primary action and core message still register, hierarchy is working. If everything blurs into one tone, hierarchy is broken.
- **Color & font restraint.** 1 dark text + 1 light/muted text + the orange accent (play / primary action) + at most one secondary accent. Adding more colors degrades scannability faster than it adds information.

## 8. No redundant elements

Apply the rip-it-out test: remove the border, the arrow, the label. If the design still communicates clearly, leave it out. Watchlist for this app:

- Chevrons on list rows (Material `ListItem` provides these — don't double up)
- Card borders when elevation or background color already separates them
- Section headers where content below is self-labeling

## See also
- [[wiki/app/navigation]] — screen-by-screen layout (primary downstream of these principles)
- [[wiki/app/prd]] — product scope
- [[wiki/sources/kole-jain-genius-ux]]
- [[wiki/sources/kole-jain-beginner-mistakes]]
- [[wiki/sources/sajid-top-tier-web-design]]
