---
title: "The Easy Way to Design Top Tier Websites"
type: source
aliases: ["Sajid Top Tier Web Design", "Top Tier Web Design Guide"]
tags: [app/design, app/ux, source/video]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-05-13
---

# The Easy Way to Design Top Tier Websites (Sajid)

- **Type:** Video / tutorial (Korean-language summary in `raw/Top_Tier_Web_Design_Guide.md`)
- **Year:** Unknown (summary referenced 2026)
- **Channel:** Sajid (YouTube)
- **Population / N:** n/a — design tutorial
- **Intervention:** n/a
- **Comparator:** n/a
- **Primary outcome:** n/a
- **Result:** n/a
- **Risk of bias / quality:** n/a — practitioner design guidance, not research. Web-focused (CSS / REM); principles translate to mobile but unit conventions don't.
- **Updates this caused in the wiki:** [[wiki/app/ux-principles]]

## Key findings

- **Less is more — start from the core action.** Don't begin a screen by designing the header or chrome. Begin by asking "what is the single core function of this page?" For a landing page: headline + input + button. Strip everything else and add back only what earns its place.
- **Gestalt: similarity & proximity for scannability.** Group similar elements visually (shape/size/color); place related elements physically close. Goal: the screen is readable in a few seconds of scanning, not careful reading.
- **Use more whitespace than feels right.** Spacing that looks correct while zoomed into one element often feels cramped at full-screen scale. Method: start with overgenerous spacing, then tighten — not the reverse.
- **Spacing system as multiples of 4** (4, 8, 12, 16, 20…). On web, use REM (px / 16) so spacing scales with the user's root font size. On mobile, the equivalent discipline is a strict dp scale and respecting the user's text-scale preference.
- **Color & font restraint.** 1 dark text color + 1 light text color + ~2 brand accents is enough. More choices degrade consistency.
- **Line-height scales inversely with font size.** Smaller text needs more line-height for legibility; larger headings can use tighter line-height.
- **Hierarchy via de-emphasis, not just emphasis.** Often more effective to lower the contrast of secondary text (gray it down) than to enlarge the primary element. The relative gap is what the eye reads.
- **Zoom-out test.** Shrink the design (or step back from the screen). If the core message and primary action still register, hierarchy is working. If everything blurs into one gray mass, hierarchy is failing.
- **Creative process — incubation matters.** Stuck on a layout? Walk away. The subconscious resolves visual problems during breaks. Don't grind through a block.
- **Inspiration before blank canvas.** Don't design from nothing. Mine references first (Mobbin, Figma Community, real apps in the relevant vertical), then recombine for your context. Copying is failure; recombination is craft.

## Quotes worth preserving

> "처음부터 헤더나 전체 구조를 고민하기보다, '이 페이지의 핵심 기능이 무엇인가?'를 먼저 생각하세요."
> (Before agonizing over the header or overall structure, first ask: "what is the core function of this page?")

> "때로는 중요한 것을 키우는 것보다, 덜 중요한 것의 대비를 낮추는 것이 더 효과적입니다."
> (Sometimes it's more effective to lower the contrast of the less-important element than to enlarge the important one.)

## Quality concerns

YouTube-tier practitioner content with no research backing. The principles themselves (Gestalt grouping, hierarchy via contrast, whitespace, restricted color palettes) are well-established in mainstream design literature — Sajid is restating them, not discovering them. Treat as a useful checklist, not a primary authority. Where this source conflicts with [[wiki/sources/kole-jain-genius-ux]] or [[wiki/sources/kole-jain-beginner-mistakes]], they take precedence (more developed framings).

## My / our take

Mostly overlaps with [[wiki/sources/kole-jain-genius-ux]] (north-star intent, design systems, progressive disclosure cousins). Distinct additions for this app:

1. **Formalize a dp spacing scale.** [[wiki/app/ux-principles]] already says "8 dp base"; this source's multiples-of-4 framing (4, 8, 12, 16, 20, 24) is more permissive and matches Material's 4 dp half-step. Adopt 4 dp grid, but reserve 4 dp gaps for *tight* contexts (icon-to-text inside a chip), defaulting to 8 dp and up.
2. **De-emphasis as a hierarchy tool.** Current 홈 design uses size to highlight the listening ring; consider whether secondary copy (e.g. TFI status row, recent diary timestamp) should also drop in contrast to push the ring forward without enlarging it further.
3. **Zoom-out test as a review ritual.** Cheap, fast check before shipping a screen — preview at 25–33 % in Android Studio's design preview. If the primary action doesn't survive the shrink, the hierarchy needs work.
4. **Incubation.** Not actionable as a design rule but worth naming as permission — when a screen redesign stalls, stepping away is the right move, not pushing through.

The web-specific bits (REM, line-height-for-small-text) translate to Compose only loosely — Material already encodes line-height in its `Typography` scale, and dp is non-negotiable on Android. Don't import the units; import the spirit.
